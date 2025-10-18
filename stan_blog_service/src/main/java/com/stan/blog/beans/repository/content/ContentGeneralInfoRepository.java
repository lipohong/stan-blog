package com.stan.blog.beans.repository.content;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.stan.blog.beans.entity.content.ContentGeneralInfoEntity;

@Repository
public interface ContentGeneralInfoRepository extends JpaRepository<ContentGeneralInfoEntity, String>,
        JpaSpecificationExecutor<ContentGeneralInfoEntity> {
}