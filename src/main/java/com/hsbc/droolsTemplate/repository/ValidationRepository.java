package com.hsbc.droolsTemplate.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hsbc.droolsTemplate.entity.Validation;

@Repository
public interface ValidationRepository extends MongoRepository<Validation, String> {
	
//	List<Validation> findAll();


}
