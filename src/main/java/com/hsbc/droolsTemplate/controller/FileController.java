package com.hsbc.droolsTemplate.controller;

import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.hsbc.droolsTemplate.model.ResponseModel;
import com.hsbc.droolsTemplate.service.FileService;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/uploadFile")
    public ResponseEntity<ResponseModel> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            return fileService.storeFile(file);
        } catch (Exception e) {
            String errorMessage = "Could not upload the file: " + e.getMessage();
            return ResponseEntity.ok(new ResponseModel("", errorMessage, false, new ArrayList<>()));
        }
    }
    @PostMapping("/convert")
    public ResponseEntity<?> convertExcelToDrl(@RequestParam("file") MultipartFile file) {
        return fileService.convertExcelToDrl(file);
    }
    
    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadExcel() {
        Workbook workbook = fileService.createTemplate();
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ruleExcel.xlsx\"")
            .body(outputStream -> workbook.write(outputStream));
    }
}
