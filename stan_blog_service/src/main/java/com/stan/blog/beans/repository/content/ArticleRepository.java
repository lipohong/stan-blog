package com.stan.blog.beans.repository.content;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stan.blog.beans.entity.content.ArticleEntity;

@Repository
public interface ArticleRepository extends JpaRepository<ArticleEntity, String> {
}