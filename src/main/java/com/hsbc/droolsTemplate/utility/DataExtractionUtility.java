package com.hsbc.droolsTemplate.utility;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.hsbc.droolsTemplate.constants.Constant;
import com.hsbc.droolsTemplate.model.Payment;

public class DataExtractionUtility {

	public static Map<String, Payment> parseSwiftFile(byte[] data) {
		Map<String, Payment> payment = new HashMap<String, Payment>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data)))) {
			StringBuilder content = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				content.append(line).append("\n");
			}
			String[] lines = content.toString().split("\n");

			for (String ln : lines) {
				if (ln.startsWith(":20:")) {
					payment.put("TransactionReferenceNumber_20", new Payment("TransactionReferenceNumber_20",
							"transactionReferenceNumber", Constant.DATA_TYPE_STRING, ln.substring(4).trim()));
				} else if (ln.startsWith(":50K:")) {
					payment.put("Sender_50K",
							new Payment("Sender_50K", "SenderName", Constant.DATA_TYPE_STRING, extractName(lines, ln)));
				} else if (ln.startsWith(":59:")) {
					payment.put("Reciever_59", new Payment("Reciever_59", "ReceiverName", Constant.DATA_TYPE_STRING,
							extractName(lines, ln)));
				} else if (ln.startsWith(":32A:")) {
					payment.put("Currency_32A",
							new Payment("Currency_32A", "Currency", Constant.DATA_TYPE_STRING, extractCurrency(ln)));
					long amount = extractAmount(ln);
					payment.put("Amount_32A",
							new Payment("Amount_32A", "Amount", Constant.DATA_TYPE_LONG, String.valueOf(amount)));
				} else if (ln.startsWith(":23B:")) {
					payment.put("BankOperationCode_23B", new Payment("ankOperationCode_23B", "BankOperationCode",
							Constant.DATA_TYPE_STRING, ln.substring(5).trim()));
				} else if (ln.startsWith(":79:")) {
					payment.put("AdditionalInfo_79", new Payment("AdditionalInfo_79", "AdditionalInfo",
							Constant.DATA_TYPE_STRING, ln.substring(4).trim()));
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return payment;
	}

	private static String extractCurrency(String ln) {
		return ln.substring(11, 14).trim();
	}

	private static long extractAmount(String ln) {
		String amountString = ln.substring(13).replace(",", "").replaceAll("[^0-9]", "").trim();
		System.out.println("Extracted amount string " + amountString);
		try {
			long amount = Long.parseLong(amountString);
			System.out.println("Parsed amount " + amount);
			return amount;
		} catch (NumberFormatException e) {
			System.err.println("Invalid amount format " + amountString);
			return 0L;
		}
	}

	private static String extractName(String[] lines, String currentLine) {
		StringBuilder name = new StringBuilder();
		boolean startAppending = false;
		for (String line : lines) {
			if (line.equals(currentLine)) {
				startAppending = true;
				continue;
			}
			if (startAppending && !line.startsWith(":")) {
				name.append(line.trim()).append(" ");
			} else if (startAppending && line.startsWith(":")) {
				break;
			}
		}
		return name.toString().trim();
	}

	public static String convertExcelToDrl(java.io.File file) throws IOException {
		StringBuilder drlContent = new StringBuilder();
		Map<String, StringBuilder> variableConditionsMap = new HashMap<>();
		Map<String, String> variableActionsMap = new HashMap<>();

		try (FileInputStream fis = new FileInputStream(file)) {
			Workbook workbook = WorkbookFactory.create(fis);
			Sheet sheet = workbook.getSheetAt(0);

			// Start from the second row to skip the header
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null)
					continue;

				Cell variableCell = row.getCell(0);
				Cell ruleCell = row.getCell(1);
				Cell valueCell = row.getCell(2);
				Cell actionCell = row.getCell(3);

				// Check if any column is empty
				if (isCellEmpty(variableCell) || isCellEmpty(ruleCell) || isCellEmpty(valueCell)
						|| isCellEmpty(actionCell)) {
					return "Please check the file format and correct it.";
				}

				String variable = getCellValueAsString(variableCell);
				String rule = getCellValueAsString(ruleCell);
				String value = getCellValueAsString(valueCell);
				String action = getCellValueAsString(actionCell);

				// Collect conditions for each variable
				variableConditionsMap.putIfAbsent(variable, new StringBuilder());
				if (variableConditionsMap.get(variable).length() > 0) {
					variableConditionsMap.get(variable).append(" || ");
				}
				variableConditionsMap.get(variable).append(variable).append(" ")
						.append(convertRuleToExpression(rule, value));

				// Store the action for the variable
				variableActionsMap.put(variable, action);
			}

			// Generate DRL rules
			for (Map.Entry<String, StringBuilder> entry : variableConditionsMap.entrySet()) {
				String variable = entry.getKey();
				String conditions = entry.getValue().toString();
				String action = variableActionsMap.get(variable);

				// Positive rule
				drlContent.append("rule \"").append(variable).append(" Positive Rule\"\n").append("    when\n")
						.append("        ").append(conditions).append("\n").append("    then\n")
						.append("        System.out.println(\"").append(action).append("\");\n").append("end\n\n");

				// Negative rule
				drlContent.append("rule \"").append(variable).append(" Negative Rule\"\n").append("    when\n")
						.append("        not (").append(conditions).append(")\n").append("    then\n")
						.append("        System.out.println(\"Invalid ").append(variable).append("\");\n")
						.append("end\n\n");
			}
		}
		return drlContent.toString();
	}

	private static String getCellValueAsString(Cell cell) {
		switch (cell.getCellType()) {
		case STRING:
			return cell.getStringCellValue();
		case BOOLEAN:
			return Boolean.toString(cell.getBooleanCellValue());
		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				return cell.getDateCellValue().toString();
			} else {
				return Double.toString(cell.getNumericCellValue());
			}
		case FORMULA:
			return cell.getCellFormula();
		case BLANK:
			return "";
		default:
			return "";
		}
	}

	private static String convertRuleToExpression(String rule, String value) {
		// Determine the operator based on the rule
		if (rule.toLowerCase().contains("greaterThanOrEqual")) {
			return Constant.GREATER_THAN_EQUALS + " " + value;
		} else if (rule.toLowerCase().contains("greater than")) {
			return Constant.GREATER_THAN + " " + value;
		} else if (rule.toLowerCase().contains("lessThanOrEqual")) {
			return Constant.LESS_THAN_EQUALS + " " + value;
		} else if (rule.toLowerCase().contains("lessThan")) {
			return Constant.LESS_THAN + " " + value;
		} else if (rule.toLowerCase().contains("equals")) {
			return Constant.EQUALS + " " + value;
		} else if (rule.toLowerCase().contains("notEquals")) {
			return Constant.NOT_EQUALS + " " + value;
		} else if (rule.toLowerCase().contains("is not null")) {
			return Constant.NOT_EQUALS + " null";
		} else {
			return rule + " " + value; // Return the original rule with value if no match is found
		}
	}

	private static boolean isCellEmpty(Cell cell) {
		return cell == null || cell.getCellType() == CellType.BLANK || getCellValueAsString(cell).trim().isEmpty();
	}

	public static Workbook createTemplate() {
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Sheet1");

		// Create header row
		Row headerRow = sheet.createRow(0);
		String[] headers = { "FeildName", "RuleName", "Value", "Action" };
		CellStyle headerStyle = workbook.createCellStyle();
		org.apache.poi.ss.usermodel.Font font = workbook.createFont();
		font.setBold(true);
		headerStyle.setFont(font);
		headerStyle.setFillForegroundColor(new XSSFColor(new Color(169, 208, 142), null));
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(headerStyle);
		}

		// Create hidden sheet with dropdown values
		Sheet hidden = workbook.createSheet("hidden");

		// Define the data for the dropdowns
		String[] stringRules = { "equals", "contains", "startsWith", "endsWith", "exists", "notEquals" };
		String[] longRules = { "lessThan", "lessThanOrEqual", "greaterThan", "greaterThanOrEqual", "exists" };
		String[] primaryDropdownValues = { "TransactionReferenceNumber_20", "Reciever_59", "AdditionalInfo_79",
				"Sender_50K", "BankOperationCode_23B", "Currency_32A", "Amount_32A" };
		Map<String, String[]> secondaryDropdownValues = new HashMap<>();
		secondaryDropdownValues.put("TransactionReferenceNumber_20", stringRules);
//		secondaryDropdownValues.put("Reciever_59", stringRules);
		secondaryDropdownValues.put("AdditionalInfo_79", stringRules);
		secondaryDropdownValues.put("Sender_50K", stringRules);
		secondaryDropdownValues.put("BankOperationCode_23B", stringRules);
		secondaryDropdownValues.put("Currency_32A", stringRules);
		secondaryDropdownValues.put("Amount_32A", longRules);

		// Populate hidden sheet with primary dropdown values
		for (int i = 0; i < primaryDropdownValues.length; i++) {
			hidden.createRow(i).createCell(0).setCellValue(primaryDropdownValues[i]);
		}

		// Populate hidden sheet with secondary dropdown values
		int rowIndex = primaryDropdownValues.length + 1;
		for (Map.Entry<String, String[]> entry : secondaryDropdownValues.entrySet()) {
			Row row = hidden.createRow(rowIndex++);
			row.createCell(0).setCellValue(entry.getKey());
			for (int i = 0; i < entry.getValue().length; i++) {
				row.createCell(i + 1).setCellValue(entry.getValue()[i]);
			}
		}

		// Define names for dropdown data
		Name primaryName = workbook.createName();
		primaryName.setNameName("primaryDropdown");
		primaryName.setRefersToFormula("hidden!$A$1:$A$" + primaryDropdownValues.length);

		for (int i = 0; i < primaryDropdownValues.length; i++) {
			Name secondaryName = workbook.createName();
			secondaryName.setNameName(primaryDropdownValues[i]);
			secondaryName.setRefersToFormula("hidden!$B$" + (i + primaryDropdownValues.length + 2) + ":$D$"
					+ (i + primaryDropdownValues.length + 2));
		}

		// Create the primary dropdown
		DataValidationHelper validationHelper = sheet.getDataValidationHelper();
		DataValidationConstraint primaryConstraint = validationHelper.createFormulaListConstraint("primaryDropdown");
		CellRangeAddressList primaryAddressList = new CellRangeAddressList(1, 1000, 0, 0);
		DataValidation primaryValidation = validationHelper.createValidation(primaryConstraint, primaryAddressList);
		primaryValidation.setSuppressDropDownArrow(true);
		sheet.addValidationData(primaryValidation);

		// Create the secondary dropdown
		String formula = "INDIRECT($A2)";
		DataValidationConstraint secondaryConstraint = validationHelper.createFormulaListConstraint(formula);
		CellRangeAddressList secondaryAddressList = new CellRangeAddressList(1, 1000, 1, 1);
		DataValidation secondaryValidation = validationHelper.createValidation(secondaryConstraint,
				secondaryAddressList);
		secondaryValidation.setSuppressDropDownArrow(true);
		sheet.addValidationData(secondaryValidation);

		// Add dropdowns for "Value" and "Action" columns
		// for values from DB
		String[] valueDropdownValues = { "Value1", "Value2", "Value3" };
		String[] actionDropdownValues = { "exists", "Set Status False", "set Status True" };
		for (int i = 0; i < valueDropdownValues.length; i++) {
			hidden.createRow(i + rowIndex).createCell(0).setCellValue(valueDropdownValues[i]);
		}
		rowIndex += valueDropdownValues.length;

		for (int i = 0; i < actionDropdownValues.length; i++) {
			hidden.createRow(i + rowIndex).createCell(0).setCellValue(actionDropdownValues[i]);
		}

		Name valueName = workbook.createName();
		valueName.setNameName("valueDropdown");
		valueName.setRefersToFormula("hidden!$A$" + (rowIndex - valueDropdownValues.length + 1) + ":$A$" + rowIndex);

		Name actionName = workbook.createName();
		actionName.setNameName("actionDropdown");
		actionName
				.setRefersToFormula("hidden!$A$" + (rowIndex + 1) + ":$A$" + (rowIndex + actionDropdownValues.length));

		DataValidationConstraint valueConstraint = validationHelper.createFormulaListConstraint("valueDropdown");
		CellRangeAddressList valueAddressList = new CellRangeAddressList(1, 1000, 2, 2);
		DataValidation valueValidation = validationHelper.createValidation(valueConstraint, valueAddressList);
		valueValidation.setSuppressDropDownArrow(true);
		sheet.addValidationData(valueValidation);

		DataValidationConstraint actionConstraint = validationHelper.createFormulaListConstraint("actionDropdown");
		CellRangeAddressList actionAddressList = new CellRangeAddressList(1, 1000, 3, 3);
		DataValidation actionValidation = validationHelper.createValidation(actionConstraint, actionAddressList);
		actionValidation.setSuppressDropDownArrow(true);
		sheet.addValidationData(actionValidation);

		// Hide the hidden sheet
		workbook.setSheetHidden(workbook.getSheetIndex(hidden), true);

		return workbook;
	}

	public void writeWorkbookToFile(Workbook workbook, String filePath) throws IOException {
		try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
			workbook.write(fileOut);
		}
	}
}
