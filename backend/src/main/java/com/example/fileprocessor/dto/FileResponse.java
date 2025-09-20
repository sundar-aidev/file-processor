package com.example.fileprocessor.dto;

import java.time.Instant;
import java.util.UUID;

public record FileResponse(
        UUID id,
        String filename,
        String fileType,
        long lines,
        long words,
        Instant timestamp
) {}
