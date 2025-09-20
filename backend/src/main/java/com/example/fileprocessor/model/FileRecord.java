package com.example.fileprocessor.model;

import java.time.Instant;
import java.util.UUID;

public record FileRecord(
        UUID id,
        String name,
        String type,
        long lines,
        long words,
        Instant createdAt
) {}
