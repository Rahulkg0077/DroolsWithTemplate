package com.hsbc.droolsTemplate.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hsbc.droolsTemplate.entity.Rule;

@Repository
public interface RulesRepository extends MongoRepository<Rule, String> {
	
	Rule findByFileType(String fileType);

}
