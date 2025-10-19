package com.stan.blog.beans.dto.file;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class FileResourceDTO {
    private Long id;
    private String originalFilename;
    private String storedFilename;
    private Long sizeInBytes;
    private String contentType;
    private Long ownerId;
    private Boolean publicToAll;
    private String downloadUrl;
    private String viewUrl;
    private String srcId;
    private String fileType;
    private Timestamp createTime;
}

