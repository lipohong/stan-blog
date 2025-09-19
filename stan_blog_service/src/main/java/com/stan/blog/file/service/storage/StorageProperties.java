package com.stan.blog.file.service.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    private String baseDir = "uploads"; // default relative to working dir
    private String publicBaseUrl = "/v1/files"; // used to build download links
}

