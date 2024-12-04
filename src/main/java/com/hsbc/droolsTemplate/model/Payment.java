package com.hsbc.droolsTemplate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    private String feildName;
    private String feildAlias;
    private String dataType;
    private String value;

    public String getFeildName() {
        return feildName;
    }

    public void setFeildName(String feildName) {
        this.feildName = feildName;
    }

    public String getFeildAlias() {
        return feildAlias;
    }

    public void setFeildAlias(String feildAlias) {
        this.feildAlias = feildAlias;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
}
