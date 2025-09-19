package com.stan.blog.file.service.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalStorageService implements StorageService {

    private final StorageProperties properties;

    @Override
    public StoredFile store(MultipartFile file, String subDirectory, String desiredFilename) throws IOException {
        String cleanOriginal = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String ext = "";
        int dot = cleanOriginal.lastIndexOf('.');
        if (dot > 0 && dot < cleanOriginal.length() - 1) {
            ext = cleanOriginal.substring(dot); // include dot
        }
        String filename = StringUtils.hasText(desiredFilename) ? desiredFilename : UUID.randomUUID().toString().replace("-", "");
        if (StringUtils.hasText(ext)) {
            filename = filename + ext.toLowerCase();
        }

        LocalDate now = LocalDate.now();
        Path base = Paths.get(properties.getBaseDir()).toAbsolutePath().normalize();
        Path targetDir = base
                .resolve(now.getYear() + "")
                .resolve(String.format("%02d", now.getMonthValue()))
                .resolve(String.format("%02d", now.getDayOfMonth()));
        if (StringUtils.hasText(subDirectory)) {
            targetDir = targetDir.resolve(subDirectory);
        }
        Files.createDirectories(targetDir);

        Path target = targetDir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        log.info("Stored file to {}", target);
        return new StoredFile(filename, target);
    }
}

