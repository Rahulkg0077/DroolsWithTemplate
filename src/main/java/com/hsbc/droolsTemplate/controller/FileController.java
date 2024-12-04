package com.hsbc.droolsTemplate.controller;

import com.example.hsbc.droolsTemplate.service.FileService;
import com.hsbc.droolsTemplate.model.ResponseModel;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
}
