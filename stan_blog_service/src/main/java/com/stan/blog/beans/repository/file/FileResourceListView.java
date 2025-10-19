package com.stan.blog.beans.repository.file;

import java.sql.Timestamp;

/**
 * Projection for listing file resources without referencing missing columns like file_type.
 */
public interface FileResourceListView {
    Long getId();
    String getOriginalFilename();
    String getStoredFilename();
    Long getSizeInBytes();
    String getContentType();
    Long getOwnerId();
    Boolean getPublicToAll();
    String getSrcId();
    String getStoragePath();
    String getChecksum();
    Timestamp getCreateTime();
}