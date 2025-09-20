package com.example.fileprocessor.service;

import com.example.fileprocessor.config.FileConfig;
import com.example.fileprocessor.dto.FileResponse;
import com.example.fileprocessor.exception.NotFoundException;
import com.example.fileprocessor.exception.UnsupportedFileTypeException;
import com.example.fileprocessor.model.FileRecord;
import com.example.fileprocessor.repository.FileRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Keep the logic boring and readable. We intentionally avoid a "perfect" CSV
 * parser here; a simple split covers the assignment and is easy to audit.
 * If someone uploads quoted commas or newlines inside cells, we'll document
 * the limitation and upgrade to a real CSV parser later.
 */
@Service
public class FileServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    private final FileRepo repo;
    private final Set<String> allowed;

    public FileServiceImpl(FileRepo repo, FileConfig cfg) {
        this.repo = repo;
        this.allowed = Set.copyOf(cfg.getAllowedFileExtensions());
    }

    @Override
    public FileResponse processFile(MultipartFile file) {
        final String name = file != null ? file.getOriginalFilename() : null;
        log.info("received name={} size={} contentType={}", name, file != null ? file.getSize() : -1, file != null ? file.getContentType() : "n/a");

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file content received.");
        }

        final String ext = extension(name);
        validate(ext);

        try {
            log.info("processed_start name={}", name);
            Counts c = count(file, ext);
            log.info("processed_end name={} lines={} words={}", name, c.lines, c.words);

            FileRecord saved = repo.save(new FileRecord(
                    UUID.randomUUID(), name, ext, c.lines, c.words, Instant.now()
            ));

            return toDto(saved);
        } catch (IOException e) {
            // We hide the stack trace from clients but keep it in logs.
            log.error("io_error name={}", name, e);
            throw new RuntimeException("Failed to process uploaded file.");
        }
    }

    @Override
    public List<FileResponse> list() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public FileResponse get(UUID id) {
        return repo.findById(id).map(this::toDto)
                .orElseThrow(() -> new NotFoundException("File not found: " + id));
    }

    // --- Helpers ---

    private FileResponse toDto(FileRecord r) {
        return new FileResponse(r.id(), r.name(), r.type(), r.lines(), r.words(), r.createdAt());
    }

    private String extension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            throw new UnsupportedFileTypeException("Uploaded file must have a valid extension.");
        }
        String ext = StringUtils.getFilenameExtension(filename);
        if (!StringUtils.hasText(ext)) {
            throw new UnsupportedFileTypeException("Uploaded file must have a valid extension.");
        }
        return ext.toLowerCase(Locale.ROOT);
    }

    private void validate(String ext) {
        if (!allowed.contains(ext)) {
            throw new UnsupportedFileTypeException("Unsupported file type: " + ext + ". Allowed: " + allowed);
        }
        log.info("validated extension={}", ext);
    }

    private Counts count(MultipartFile file, String ext) throws IOException {
        long lines = 0, words = 0;
        try (var is = file.getInputStream();
             var reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            final boolean csv = "csv".equalsIgnoreCase(ext);
            String line;
            while ((line = reader.readLine()) != null) {
                lines++;
                if (csv) {
                    // Pragmatic CSV: split by comma, ignore blank cells.
                    for (String cell : line.split(",", -1)) {
                        if (StringUtils.hasText(cell.trim())) words++;
                    }
                } else {
                    String trimmed = line.trim();
                    if (StringUtils.hasText(trimmed)) {
                        words += trimmed.split("\s+").length;
                    }
                }
            }
        }
        return new Counts(lines, words);
    }

    private record Counts(long lines, long words) {}
}
