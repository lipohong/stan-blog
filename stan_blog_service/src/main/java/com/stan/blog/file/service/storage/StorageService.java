package com.stan.blog.file.service.storage;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    StoredFile store(MultipartFile file, String subDirectory, String desiredFilename) throws IOException;

    record StoredFile(String storedFilename, Path absolutePath) {}
}

