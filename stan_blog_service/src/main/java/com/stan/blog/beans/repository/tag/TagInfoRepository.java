package com.stan.blog.beans.repository.tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stan.blog.beans.entity.tag.TagInfoEntity;

@Repository
public interface TagInfoRepository extends JpaRepository<TagInfoEntity, Long> {
}