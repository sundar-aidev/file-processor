package com.example.fileprocessor.controller;

import com.example.fileprocessor.dto.FileResponse;
import com.example.fileprocessor.service.FileService;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/files", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class FileController {
    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    private final FileService service;

    public FileController(FileService service) { this.service = service; }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponse> upload(@RequestParam("file") @NotNull MultipartFile file) {
        log.info("endpoint upload");
        return ResponseEntity.ok(service.processFile(file));
    }

    @GetMapping
    public List<FileResponse> list() {
        log.info("endpoint list");
        return service.list();
    }

    @GetMapping("/{id}")
    public FileResponse get(@PathVariable UUID id) {
        log.info("endpoint get id={}", id);
        return service.get(id);
    }

    @GetMapping(path = "/", produces = MediaType.TEXT_PLAIN_VALUE)
    public String root() { return "File Processor is running. POST /api/v1/files/upload"; }
}
