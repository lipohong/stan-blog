package com.stan.blog.beans.repository.content;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stan.blog.beans.entity.content.ContentAdminEntity;

@Repository
public interface ContentAdminRepository extends JpaRepository<ContentAdminEntity, String> {

    List<ContentAdminEntity> findByBannedTrue();

    List<ContentAdminEntity> findByRecommendedTrue();
}