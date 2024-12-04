package com.hsbc.droolsTemplate.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.hsbc.droolsTemplate.model.Payment;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "fileMetadata")
public class File {

    public String getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public Map<String, Payment> getFileContentMap() {
        return fileContentMap;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setFileContentMap(Map<String, Payment> fileContentMap) {
        this.fileContentMap = fileContentMap;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Id
    private String id;
    private String fileName;
    private String fileType;
    private Map<String, Payment> fileContentMap = new HashMap<String, Payment>();


    @Field("data")
    private byte[] data;

    public String getFileContentAsString() {
        return new String(data, StandardCharsets.UTF_8);
    }
}
