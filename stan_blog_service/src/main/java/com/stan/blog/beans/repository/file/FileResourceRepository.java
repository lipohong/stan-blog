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

    @org.springframework.data.jpa.repository.Query(
        "select e.id as id, e.originalFilename as originalFilename, e.storedFilename as storedFilename, e.sizeInBytes as sizeInBytes, " +
        "e.contentType as contentType, e.ownerId as ownerId, e.publicToAll as publicToAll, e.createTime as createTime from FileResourceEntity e " +
        "where e.ownerId = ?1 and e.deleted = false order by e.id desc")
    Page<FileResourceListView> findViewByOwnerIdAndDeletedFalse(Long ownerId, Pageable pageable);
}
