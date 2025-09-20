package com.example.fileprocessor.repository;

import com.example.fileprocessor.model.FileRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryRepo implements FileRepo {
    private static final Logger log = LoggerFactory.getLogger(InMemoryRepo.class);

    private final ConcurrentHashMap<UUID, FileRecord> store = new ConcurrentHashMap<>();

    @Override
    public FileRecord save(FileRecord record) {
        store.put(record.id(), record);
        log.info("saved id={} name={} lines={} words={} at={}", record.id(), record.name(), record.lines(), record.words(), record.createdAt());
        return record;
    }

    @Override
    public Optional<FileRecord> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<FileRecord> findAll() {
        return List.copyOf(new ArrayList<>(store.values()));
    }
}
