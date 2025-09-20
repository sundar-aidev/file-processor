package com.example.fileprocessor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app")
public class FileConfig {
    private List<String> allowedFileExtensions = List.of("txt","csv");

    public List<String> getAllowedFileExtensions() {
        return List.copyOf(allowedFileExtensions);
    }
    public void setAllowedFileExtensions(List<String> allowedFileExtensions) {
        this.allowedFileExtensions = allowedFileExtensions;
    }
}
