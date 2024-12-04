package com.hsbc.droolsTemplate.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hsbc.droolsTemplate.entity.File;

import java.util.List;

@Repository
public interface FileRepository extends MongoRepository<File, String> {

    List<File> findByFileType(String fileType);

    File findByFileName(String name);
}
