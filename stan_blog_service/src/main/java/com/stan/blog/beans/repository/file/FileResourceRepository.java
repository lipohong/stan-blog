package com.stan.blog.beans.repository.file;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stan.blog.beans.entity.file.FileResourceEntity;

@Repository
public interface FileResourceRepository extends JpaRepository<FileResourceEntity, Long> {

    Page<FileResourceEntity> findByOwnerIdAndDeletedFalse(Long ownerId, Pageable pageable);
    Page<FileResourceEntity> findByOwnerIdAndSrcIdAndFileTypeAndDeletedFalse(Long ownerId, String srcId, String fileType, Pageable pageable);
    Page<FileResourceEntity> findByOwnerIdAndSrcIdAndDeletedFalse(Long ownerId, String srcId, Pageable pageable);
}
