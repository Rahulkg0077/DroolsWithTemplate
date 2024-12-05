package com.hsbc.droolsTemplate.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hsbc.droolsTemplate.entity.File;
import com.hsbc.droolsTemplate.entity.Validation;
import com.hsbc.droolsTemplate.model.Payment;
import com.hsbc.droolsTemplate.model.ResponseModel;
import com.hsbc.droolsTemplate.repository.FileRepository;
import com.hsbc.droolsTemplate.repository.ValidationRepository;
import com.hsbc.droolsTemplate.service.FileService;
import com.hsbc.droolsTemplate.utility.DataExtractionUtility;

@Service
public class FileServiceImpl implements FileService {

	@Autowired
	private FileRepository fileRepository;

	private final ValidationRepository validationRepository;

	@Autowired
	public FileServiceImpl(ValidationRepository validationRepository) {
		this.validationRepository = validationRepository;
	}

	@Override
	public ResponseEntity<ResponseModel> storeFile(MultipartFile file) throws IOException {
		File model = new File();
		String str = returnFileType(Objects.requireNonNull(file.getOriginalFilename()));
		model.setFileType(str);
		model.setFileName(file.getOriginalFilename());
		model.setData(file.getBytes());
		Map<String, Payment> paymentMap = DataExtractionUtility.parseSwiftFile(model.getData());
		model.setFileContentMap(paymentMap);
		ResponseModel response;
		List<Validation> validations = validationRepository.findAll();
		List<String> validationErrors = validateFields(paymentMap, validations);
		if (null != validationErrors || !CollectionUtils.isEmpty(validationErrors)) {
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
	    Validation validation = validations.stream()
	        .filter(v -> v.getFieldName().equals(fieldName))
	        .findFirst()
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

	private String returnFileType(String fileType) {
		int lastIndex = fileType.lastIndexOf(".");
		return fileType.substring(lastIndex + 1);
	}
}
