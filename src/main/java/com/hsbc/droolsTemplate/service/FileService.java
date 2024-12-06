package com.hsbc.droolsTemplate.service;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hsbc.droolsTemplate.model.ResponseModel;

import java.io.IOException;

@Service
public interface FileService {

    ResponseEntity<ResponseModel> storeFile(MultipartFile file) throws IOException;

	ResponseEntity<?> convertExcelToDrl(MultipartFile file);

	Workbook createTemplate();
}
