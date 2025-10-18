package com.stan.blog.file.service.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class LocalStorageServiceTests {

    @TempDir
    Path tempDir;

    private LocalStorageService storageService;
    private StorageProperties properties;

    @BeforeEach
    void setUp() {
        properties = new StorageProperties();
        properties.setBaseDir(tempDir.toString());
        storageService = new LocalStorageService(properties);
    }

    @Test
    void storePersistsFileUnderDatedDirectory() throws IOException {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "screenshot.PNG",
                "image/png",
                "image-bytes".getBytes());

        StorageService.StoredFile storedFile = storageService.store(multipartFile, null, null);

        LocalDate today = LocalDate.now();
        Path expectedDirectory = tempDir
                .resolve(String.valueOf(today.getYear()))
                .resolve(String.format("%02d", today.getMonthValue()))
                .resolve(String.format("%02d", today.getDayOfMonth()));

        assertTrue(Files.exists(expectedDirectory));
        Path persistedFile = expectedDirectory.resolve(storedFile.storedFilename());
        assertTrue(Files.exists(persistedFile));
        assertEquals("image-bytes", Files.readString(persistedFile));
        assertTrue(storedFile.storedFilename().endsWith(".png"));
    }

    @Test
    void storeUsesDesiredNameAndSubDirectory() throws IOException {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "design.DOCX",
                "application/vnd.openxmlformats",
                "document".getBytes());

        StorageService.StoredFile storedFile = storageService.store(multipartFile, "attachments", "proposal");

        LocalDate today = LocalDate.now();
        Path expectedDirectory = tempDir
                .resolve(String.valueOf(today.getYear()))
                .resolve(String.format("%02d", today.getMonthValue()))
                .resolve(String.format("%02d", today.getDayOfMonth()))
                .resolve("attachments");

        assertTrue(Files.exists(expectedDirectory));
        assertEquals("proposal.docx", storedFile.storedFilename());
        Path persistedFile = expectedDirectory.resolve(storedFile.storedFilename());
        assertTrue(Files.exists(persistedFile));
        assertEquals("document", Files.readString(persistedFile));
    }
}
