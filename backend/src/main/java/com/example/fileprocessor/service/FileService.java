package com.example.fileprocessor.service;

import com.example.fileprocessor.dto.FileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/** Application boundary. */
public interface FileService {
    FileResponse processFile(MultipartFile file);
    List<FileResponse> list();
    FileResponse get(UUID id);
}
