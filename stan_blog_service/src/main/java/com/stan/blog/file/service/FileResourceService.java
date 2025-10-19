package com.stan.blog.file.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.stan.blog.beans.dto.file.FileResourceDTO;
import com.stan.blog.beans.entity.file.FileResourceEntity;
import com.stan.blog.beans.repository.file.FileResourceRepository;
import com.stan.blog.core.utils.AuthenticationUtil;
import com.stan.blog.core.utils.BasicConverter;
import com.stan.blog.core.utils.SecurityUtil;
import com.stan.blog.file.service.storage.StorageProperties;
import com.stan.blog.file.service.storage.StorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileResourceService {

    private final StorageService storageService;
    private final StorageProperties storageProperties;
    private final FileResourceRepository fileResourceRepository;

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
                entity.setContentType(StringUtils.hasText(file.getContentType()) ? file.getContentType()
                        : MediaType.APPLICATION_OCTET_STREAM_VALUE);
                entity.setOwnerId(user.getUserProfile().getId());
                entity.setPublicToAll(publicToAll);
                entity.setDeleted(false);
                entity.setChecksum(calcChecksum(stored.absolutePath()));

                FileResourceEntity saved = fileResourceRepository.save(entity);

                FileResourceDTO dto = BasicConverter.convert(saved, FileResourceDTO.class);
                dto.setDownloadUrl(buildDownloadUrl(saved.getId()));
                dto.setViewUrl(buildViewUrl(saved.getId()));
                return ResponseEntity.ok(dto);
            } catch (IOException ex) {
                log.error("Failed to store uploaded file", ex);
                return ResponseEntity.internalServerError().build();
            }
        }).getBody();
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> download(Long id) {
        return fileResourceRepository.findById(id)
                .filter(entity -> !Boolean.TRUE.equals(entity.getDeleted()))
                .map(entity -> {
                    if (!canAccess(entity)) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).<byte[]>build();
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
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<byte[]>build();
                    }
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).<byte[]>build());
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> viewInline(Long id) {
        return fileResourceRepository.findById(id)
                .filter(entity -> !Boolean.TRUE.equals(entity.getDeleted()))
                .map(entity -> {
                    if (!canAccess(entity)) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).<byte[]>build();
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
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<byte[]>build();
                    }
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).<byte[]>build());
    }

    @Transactional(readOnly = true)
    public Page<FileResourceDTO> getUserFiles(Long ownerId, int page, int size) {
        int resolvedPage = Math.max(page - 1, 0);
        int resolvedSize = Math.max(size, 1);
        Pageable pageable = PageRequest.of(resolvedPage, resolvedSize, Sort.by(Sort.Direction.DESC, "createTime"));
        return fileResourceRepository.findByOwnerIdAndDeletedFalse(ownerId, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<FileResourceDTO> getFilesBySource(Long ownerId, String srcId, String fileType, int page, int size) {
        int resolvedPage = Math.max(page - 1, 0);
        int resolvedSize = Math.max(size, 1);
        Pageable pageable = PageRequest.of(resolvedPage, resolvedSize, Sort.by(Sort.Direction.DESC, "createTime"));
        try {
            return fileResourceRepository
                .findByOwnerIdAndSrcIdAndFileTypeAndDeletedFalse(ownerId, srcId, fileType, pageable)
                .map(this::toDTO);
        } catch (RuntimeException ex) {
            log.warn("Fallback to query without fileType due to schema mismatch: {}", ex.getMessage());
            return fileResourceRepository
                .findByOwnerIdAndSrcIdAndDeletedFalse(ownerId, srcId, pageable)
                .map(this::toDTO);
        }
    }

    @Transactional
    public FileResourceEntity save(FileResourceEntity entity) {
        return fileResourceRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public FileResourceEntity getById(Long id) {
        return fileResourceRepository.findById(id).orElse(null);
    }

    public boolean canModify(FileResourceEntity entity) {
        var current = SecurityUtil.getCurrentUserDetail();
        if (current == null) {
            return false;
        }
        boolean isOwner = entity.getOwnerId() != null && entity.getOwnerId().equals(current.getUserProfile().getId());
        boolean isAdmin = current.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ADMIN"));
        return isOwner || isAdmin;
    }

    public boolean canAccess(FileResourceEntity entity) {
        if (Boolean.TRUE.equals(entity.getPublicToAll())) {
            return true;
        }
        var current = SecurityUtil.getCurrentUserDetail();
        if (current == null) {
            return false;
        }
        return canModify(entity);
    }

    @Transactional
    public FileResourceDTO upload(MultipartFile file, boolean publicToAll, String srcId, String fileType) {
        return AuthenticationUtil.withAuthenticatedUser(user -> {
            try {
                String subDir = String.valueOf(user.getUserProfile().getId());
                StorageService.StoredFile stored = storageService.store(file, subDir, null);

                FileResourceEntity entity = new FileResourceEntity();
                entity.setOriginalFilename(file.getOriginalFilename());
                entity.setStoredFilename(stored.storedFilename());
                entity.setStoragePath(stored.absolutePath().toString());
                entity.setSizeInBytes(file.getSize());
                entity.setContentType(StringUtils.hasText(file.getContentType()) ? file.getContentType()
                        : MediaType.APPLICATION_OCTET_STREAM_VALUE);
                entity.setOwnerId(user.getUserProfile().getId());
                entity.setPublicToAll(publicToAll);
                entity.setDeleted(false);
                entity.setChecksum(calcChecksum(stored.absolutePath()));
                entity.setSrcId(srcId);
                entity.setFileType(fileType);

                FileResourceEntity saved = fileResourceRepository.save(entity);

                FileResourceDTO dto = BasicConverter.convert(saved, FileResourceDTO.class);
                dto.setDownloadUrl(buildDownloadUrl(saved.getId()));
                dto.setViewUrl(buildViewUrl(saved.getId()));
                return ResponseEntity.ok(dto);
            } catch (IOException ex) {
                log.error("Failed to store uploaded file", ex);
                return ResponseEntity.internalServerError().build();
            }
        }).getBody();
    }

    public FileResourceDTO toDTO(FileResourceEntity entity) {
        FileResourceDTO dto = BasicConverter.convert(entity, FileResourceDTO.class);
        dto.setDownloadUrl(buildDownloadUrl(entity.getId()));
        dto.setViewUrl(buildViewUrl(entity.getId()));
        return dto;
    }

    private String buildDownloadUrl(Long id) {
        return storageProperties.getPublicBaseUrl() + "/" + id + "/download";
    }

    private String buildViewUrl(Long id) {
        return storageProperties.getPublicBaseUrl() + "/" + id + "/view";
    }

    private static String sanitizeFilename(String name) {
        if (!StringUtils.hasText(name)) {
            return "file";
        }
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
