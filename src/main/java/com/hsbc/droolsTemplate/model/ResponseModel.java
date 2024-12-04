package com.hsbc.droolsTemplate.model;

import java.util.ArrayList;
import java.util.List;


public class ResponseModel {
    private String message;
    private String additionalInfo;
    private boolean success;
    private List<String> validationErrors = new ArrayList<>();

    public ResponseModel() {
    }

    public ResponseModel(String message, String additionalInfo, boolean success, List<String> validationErrors) {
        this.message = message;
        this.additionalInfo = additionalInfo;
        this.success = success;
        this.setValidationErrors(validationErrors);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "ResponseModel{" +
                "message='" + message + '\'' +
                ", additionalInfo='" + additionalInfo + '\'' +
                ", success=" + success +
                '}';
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<String> validationErrors) {
        this.validationErrors = validationErrors;
    }
}
