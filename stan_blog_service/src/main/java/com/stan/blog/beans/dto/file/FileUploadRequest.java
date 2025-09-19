package com.stan.blog.beans.dto.file;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "FileUploadRequest", description = "Multipart form for file upload")
public class FileUploadRequest {
    @Schema(type = "string", format = "binary", description = "File to upload")
    private MultipartFile file;

    @Schema(description = "Whether the file is public to all", defaultValue = "false")
    private Boolean publicToAll;
}

