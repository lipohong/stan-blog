package com.stan.blog.beans.repository.file;

import java.sql.Timestamp;

/**
 * Minimal projection for file listing on legacy schemas (no src_id/file_type columns).
 */
public interface FileResourceListView {
    Long getId();
    String getOriginalFilename();
    String getStoredFilename();
    Long getSizeInBytes();
    String getContentType();
    Long getOwnerId();
    Boolean getPublicToAll();
    Timestamp getCreateTime();
}