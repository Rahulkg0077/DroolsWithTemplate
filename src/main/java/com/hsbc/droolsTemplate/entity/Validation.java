package com.hsbc.droolsTemplate.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.hsbc.droolsTemplate.model.ErrorMessage;

@Document(collection = "validations")
public class Validation {
	@Id
	private String id;
	private String fieldName;
	private String aliasName;
	private String dataType;
	private String description;
	private String regex;
	private Long maxLength;
	private ErrorMessage errorMessage;
	private Boolean required;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getAliasName() {
		return aliasName;
	}
	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getRegex() {
		return regex;
	}
	public void setRegex(String regex) {
		this.regex = regex;
	}
	public Long getMaxLength() {
		return maxLength;
	}
	public void setMaxLength(Long maxLength) {
		this.maxLength = maxLength;
	}
	public ErrorMessage getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(ErrorMessage errorMessage) {
		this.errorMessage = errorMessage;
	}
	public Boolean getRequired() {
		return required;
	}
	public void setRequired(Boolean required) {
		this.required = required;
	}
	public Validation(String id, String fieldName, String aliasName, String dataType, String description, String regex,
			Long maxLength, ErrorMessage errorMessage, Boolean required) {
		super();
		this.id = id;
		this.fieldName = fieldName;
		this.aliasName = aliasName;
		this.dataType = dataType;
		this.description = description;
		this.regex = regex;
		this.maxLength = maxLength;
		this.errorMessage = errorMessage;
		this.required = required;
	}
	

}
