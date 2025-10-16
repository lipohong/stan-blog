package com.stan.blog.beans.repository.file;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stan.blog.beans.entity.file.FileResourceEntity;

@Repository
public interface FileResourceRepository extends JpaRepository<FileResourceEntity, Long> {
}