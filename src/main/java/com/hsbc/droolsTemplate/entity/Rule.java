package com.hsbc.droolsTemplate.entity;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Document(collection = "rules")

@Getter
@Setter
public class Rule {
	@Id
	private String id;
	private String fileType;
	private Map<String, List<String>> rulesMap;
}
