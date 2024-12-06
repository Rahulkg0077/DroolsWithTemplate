package com.hsbc.droolsTemplate.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hsbc.droolsTemplate.entity.FileMetadata;
import com.hsbc.droolsTemplate.entity.Validation;
import com.hsbc.droolsTemplate.model.Payment;
import com.hsbc.droolsTemplate.model.ResponseModel;
import com.hsbc.droolsTemplate.repository.FileRepository;
import com.hsbc.droolsTemplate.repository.RulesRepository;
import com.hsbc.droolsTemplate.repository.ValidationRepository;
import com.hsbc.droolsTemplate.service.FileService;
import com.hsbc.droolsTemplate.utility.DataExtractionUtility;

@Service
public class FileServiceImpl implements FileService {

	@Autowired
	private FileRepository fileRepository;

	@Autowired
	private RulesRepository rulesRepository;

	private final ValidationRepository validationRepository;

	@Autowired
	public FileServiceImpl(ValidationRepository validationRepository) {
		this.validationRepository = validationRepository;
	}

	@Override
	public ResponseEntity<ResponseModel> storeFile(MultipartFile file) throws IOException {
		FileMetadata model = new FileMetadata();
		// String str =
		// returnFileType(Objects.requireNonNull(file.getOriginalFilename()));
		model.setFileType("MT999");
		model.setFileName(file.getOriginalFilename());
		model.setData(file.getBytes());
		Map<String, Payment> paymentMap = DataExtractionUtility.parseSwiftFile(model.getData());
		model.setFileContentMap(paymentMap);
		ResponseModel response;
		List<Validation> validations = validationRepository.findAll();
		List<String> validationErrors = validateFields(paymentMap, validations);
		if (!CollectionUtils.isEmpty(validationErrors)) {
			response = new ResponseModel(model.getFileName(), "Cannot upload file, feilds are not valid", false,
					validationErrors);
			return ResponseEntity.ok(response);
		} else {
			fileRepository.save(model);
			response = new ResponseModel(model.getFileName(), "File uploaded successfully", true, null);
			return ResponseEntity.ok(response);
		}

	}

	public static List<String> validateFields(Map<String, Payment> fieldValues, List<Validation> validations) {
		List<String> validationErrors = new ArrayList<String>();
		for (Map.Entry<String, Payment> entry : fieldValues.entrySet()) {
			String result = validateField(entry.getKey(), entry.getValue().getValue(), validations);
			if (result != null) {
				validationErrors.add(result);
			}
		}
		return validationErrors;
	}

	private static String validateField(String fieldName, String value, List<Validation> validations) {
		Validation validation = validations.stream().filter(v -> v.getFieldName().equals(fieldName)).findFirst()
				.orElse(null);

		if (validation == null) {
			return "Unknown field: " + fieldName;
		}

		if (value == null || value.isEmpty()) {
			return validation.getErrorMessage().getNullMessage();
		}

		if (value.length() > validation.getMaxLength()) {
			return validation.getErrorMessage().getLengthMessage();
		}

		if (!Pattern.matches(validation.getRegex(), value)) {
			return validation.getErrorMessage().getFormatMessage();
		}

		if ("Long".equals(validation.getDataType())) {
			try {
				long numericValue = Long.parseLong(value);
				if (numericValue == 0) {
					return "Amount must not be zero.";
				}
			} catch (NumberFormatException e) {
				return "Amount must be a valid number.";
			}
		}

		return null;
	}

	// not storing extension as type , Storing actual file type for now Like MT999

	/*
	 * private String returnFileType(String fileType) { int lastIndex =
	 * fileType.lastIndexOf("."); return fileType.substring(lastIndex + 1); }
	 */

	public ResponseEntity<?> convertExcelToDrl(MultipartFile file) {
		try {
			String fileName = file.getOriginalFilename();
			if (fileName == null || !(fileName.endsWith(".xlsx") || fileName.endsWith(".xls"))) {
				return new ResponseEntity<>("Invalid file type. Only .xlsx or .xls files are allowed.",
						HttpStatus.BAD_REQUEST);
			}

			String userHome = System.getProperty("user.home");
			String directoryPath = userHome + "/Downloads/";

			File directory = new File(directoryPath);
			if (!directory.exists()) {
				directory.mkdirs();
			}
			String filePath = directoryPath + "ruleExcel.xlsx";
			File convFile = new File(filePath);

			String drlContent = DataExtractionUtility.convertExcelToDrl(convFile);

			File drlFile = new File(directory, "RuleToDRL.drl");
			try (FileOutputStream fos = new FileOutputStream(drlFile)) {
				fos.write(drlContent.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}

			byte[] fileContent = Files.readAllBytes(drlFile.toPath());
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("attachment", drlFile.getName());
			headers.setContentLength(fileContent.length);

			return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);

		} catch (

		IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>("Please check the file format and correct it.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public Workbook createTemplate() {
		// here we have to add file type check So we can handle template creation logic
		// according to fileType
		Workbook workbook = DataExtractionUtility.createTemplate();
		return workbook;
	}
}
