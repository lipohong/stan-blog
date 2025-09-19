package com.stan.blog.file.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stan.blog.beans.dto.file.FileResourceDTO;
import com.stan.blog.beans.entity.file.FileResourceEntity;
import com.stan.blog.core.utils.AuthenticationUtil;
import com.stan.blog.core.utils.BasicConverter;
import com.stan.blog.core.utils.SecurityUtil;
import com.stan.blog.file.mapper.FileResourceMapper;
import com.stan.blog.file.service.storage.StorageProperties;
import com.stan.blog.file.service.storage.StorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileResourceService extends ServiceImpl<FileResourceMapper, FileResourceEntity> {

    private final StorageService storageService;
    private final StorageProperties storageProperties;

    @Transactional
    public FileResourceDTO upload(MultipartFile file, boolean publicToAll) {
        return AuthenticationUtil.withAuthenticatedUser(user -> {
            try {
                String subDir = String.valueOf(user.getUserProfile().getId());
                StorageService.StoredFile stored = storageService.store(file, subDir, null);

                FileResourceEntity entity = new FileResourceEntity();
                entity.setOriginalFilename(file.getOriginalFilename());
                entity.setStoredFilename(stored.storedFilename());
                entity.setStoragePath(stored.absolutePath().toString());
                entity.setSizeInBytes(file.getSize());
                entity.setContentType(StringUtils.hasText(file.getContentType()) ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE);
                entity.setOwnerId(user.getUserProfile().getId());
                entity.setPublicToAll(publicToAll);
                entity.setDeleted(false);
                entity.setChecksum(calcChecksum(stored.absolutePath()));
                this.save(entity);

                FileResourceDTO dto = BasicConverter.convert(entity, FileResourceDTO.class);
                dto.setDownloadUrl(buildDownloadUrl(entity.getId()));
                return ResponseEntity.ok(dto);
            } catch (IOException ex) {
                log.error("Failed to store uploaded file", ex);
                return ResponseEntity.internalServerError().build();
            }
        }).getBody();
    }

    public ResponseEntity<byte[]> download(Long id) {
        FileResourceEntity entity = this.getById(id);
        if (entity == null || Boolean.TRUE.equals(entity.getDeleted())) {
            return ResponseEntity.notFound().build();
        }
        if (!canAccess(entity)) {
            return ResponseEntity.status(401).build();
        }
        try {
            Path path = Path.of(entity.getStoragePath());
            byte[] bytes = Files.readAllBytes(path);
            String contentType = StringUtils.hasText(entity.getContentType()) ? entity.getContentType()
                    : MediaType.APPLICATION_OCTET_STREAM_VALUE;
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + sanitizeFilename(entity.getOriginalFilename()) + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(bytes.length)
                    .body(bytes);
        } catch (IOException e) {
            log.error("Failed to read file {}", entity.getStoragePath(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<byte[]> viewInline(Long id) {
        FileResourceEntity entity = this.getById(id);
        if (entity == null || Boolean.TRUE.equals(entity.getDeleted())) {
            return ResponseEntity.notFound().build();
        }
        if (!canAccess(entity)) {
            return ResponseEntity.status(401).build();
        }
        try {
            Path path = Path.of(entity.getStoragePath());
            byte[] bytes = Files.readAllBytes(path);
            String contentType = StringUtils.hasText(entity.getContentType()) ? entity.getContentType()
                    : MediaType.APPLICATION_OCTET_STREAM_VALUE;
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + sanitizeFilename(entity.getOriginalFilename()) + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(bytes.length)
                    .body(bytes);
        } catch (IOException e) {
            log.error("Failed to read file {}", entity.getStoragePath(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    public boolean canModify(FileResourceEntity entity) {
        var current = SecurityUtil.getCurrentUserDetail();
        if (current == null) return false;
        boolean isOwner = entity.getOwnerId() != null && entity.getOwnerId().equals(current.getUserProfile().getId());
        boolean isAdmin = current.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ADMIN"));
        return isOwner || isAdmin;
    }

    public boolean canAccess(FileResourceEntity entity) {
        if (Boolean.TRUE.equals(entity.getPublicToAll())) return true;
        var current = SecurityUtil.getCurrentUserDetail();
        if (current == null) return false;
        return canModify(entity);
    }

    public FileResourceDTO toDTO(FileResourceEntity entity) {
        FileResourceDTO dto = BasicConverter.convert(entity, FileResourceDTO.class);
        dto.setDownloadUrl(buildDownloadUrl(entity.getId()));
        return dto;
    }

    private String buildDownloadUrl(Long id) {
        return storageProperties.getPublicBaseUrl() + "/" + id + "/download";
    }

    private static String sanitizeFilename(String name) {
        if (!StringUtils.hasText(name)) return "file";
        return name.replaceAll("[\\\\/\\r\\n]", "_");
    }

    private static String calcChecksum(Path path) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] data = Files.readAllBytes(path);
            byte[] hash = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            return null;
        }
    }
}
