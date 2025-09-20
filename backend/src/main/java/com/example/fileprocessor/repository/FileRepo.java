package com.example.fileprocessor.repository;

import com.example.fileprocessor.model.FileRecord;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileRepo {
    FileRecord save(FileRecord record);
    Optional<FileRecord> findById(UUID id);
    List<FileRecord> findAll();
}
