package com.hsbc.droolsTemplate.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hsbc.droolsTemplate.entity.FileMetadata;

@Repository
public interface FileRepository extends MongoRepository<FileMetadata, String> {

    List<FileMetadata> findByFileType(String fileType);

    FileMetadata findByFileName(String name);
}
