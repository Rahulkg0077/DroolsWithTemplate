package com.hsbc.droolsTemplate.utility;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;

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
					payment.put("20", new Payment("20", "transactionReferenceNumber", Constant.DATA_TYPE_STRING,
							ln.substring(4).trim()));
				} else if (ln.startsWith(":50K:")) {
					payment.put("50K",
							new Payment("50K", "SenderName", Constant.DATA_TYPE_STRING, extractName(lines, ln)));
				} else if (ln.startsWith(":59:")) {
					payment.put("59",
							new Payment("59", "ReceiverName", Constant.DATA_TYPE_STRING, extractName(lines, ln)));
				} else if (ln.startsWith(":32A:")) {
					payment.put("32A-2",
							new Payment("32A-2", "Currency", Constant.DATA_TYPE_STRING, extractCurrency(ln)));
					long amount = extractAmount(ln);
					payment.put("32A-3",
							new Payment("32A-3", "Amount", Constant.DATA_TYPE_LONG, String.valueOf(amount)));
				} else if (ln.startsWith(":23B:")) {
					payment.put("23B",
							new Payment("23B", "BankOperationCode", Constant.DATA_TYPE_STRING, ln.substring(5).trim()));
				} else if (ln.startsWith(":79:")) {
					payment.put("79",
							new Payment("79", "AdditionalInfo", Constant.DATA_TYPE_STRING, ln.substring(4).trim()));
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

}
