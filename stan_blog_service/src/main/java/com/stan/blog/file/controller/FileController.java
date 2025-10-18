package com.stan.blog.file.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.stan.blog.beans.dto.file.FileResourceDTO;
import com.stan.blog.beans.dto.file.FileVisibilityUpdateDTO;
import com.stan.blog.beans.entity.file.FileResourceEntity;
import com.stan.blog.core.dto.PageResponse;
import com.stan.blog.core.utils.AuthenticationUtil;
import com.stan.blog.file.service.FileResourceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/files")
public class FileController {

    private final FileResourceService fileService;

    @Operation(
        summary = "Upload a file",
        description = "Uploads a file with optional visibility flag.",
        requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                schema = @Schema(implementation = com.stan.blog.beans.dto.file.FileUploadRequest.class)))
    )
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResourceDTO> upload(@RequestParam(value = "file", required = true) MultipartFile file,
                                                  @RequestParam(value = "publicToAll", required = false, defaultValue = "false") Boolean publicToAll) {
        return AuthenticationUtil.withAuthenticatedUser(user -> {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            boolean pub = publicToAll != null && Boolean.TRUE.equals(publicToAll);
            FileResourceDTO dto = fileService.upload(file, pub);
            return ResponseEntity.ok(dto);
        });
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        return fileService.download(id);
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<byte[]> view(@PathVariable Long id) {
        return fileService.viewInline(id);
    }

    @GetMapping
    public ResponseEntity<PageResponse<FileResourceDTO>> myFiles(@RequestParam(defaultValue = "1") int page,
                                                                 @RequestParam(defaultValue = "10") int size) {
        return AuthenticationUtil.withAuthenticatedUser(user -> {
            Page<FileResourceDTO> resultPage = fileService.getUserFiles(user.getUserProfile().getId(), page, size);
            return ResponseEntity.ok(PageResponse.from(resultPage));
        });
    }

    @PutMapping("/{id}/visibility")
    public ResponseEntity<FileResourceDTO> updateVisibility(@PathVariable Long id,
                                                            @Valid @org.springframework.web.bind.annotation.RequestBody FileVisibilityUpdateDTO dto) {
        return AuthenticationUtil.withAuthenticatedUser(user -> {
            FileResourceEntity entity = fileService.getById(id);
            if (entity == null || Boolean.TRUE.equals(entity.getDeleted())) {
                return ResponseEntity.notFound().build();
            }
            if (!fileService.canModify(entity)) {
                return ResponseEntity.status(401).build();
            }
            entity.setPublicToAll(dto.getPublicToAll());
            fileService.save(entity);
            return ResponseEntity.ok(fileService.toDTO(entity));
        });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return AuthenticationUtil.withAuthenticatedUser(user -> {
            FileResourceEntity entity = fileService.getById(id);
            if (entity == null || Boolean.TRUE.equals(entity.getDeleted())) {
                return ResponseEntity.notFound().build();
            }
            if (!fileService.canModify(entity)) {
                return ResponseEntity.status(401).build();
            }
            entity.setDeleted(true);
            fileService.save(entity);
            return ResponseEntity.ok().build();
        });
    }
}
