package com.stan.blog.beans.repository.content;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stan.blog.beans.entity.content.ContentTagEntity;

@Repository
public interface ContentTagRepository extends JpaRepository<ContentTagEntity, Long> {

    void deleteByContentId(String contentId);

    List<ContentTagEntity> findByContentId(String contentId);

    List<ContentTagEntity> findByContentIdIn(Collection<String> contentIds);

    List<ContentTagEntity> findByTagIdIn(Collection<Long> tagIds);
}