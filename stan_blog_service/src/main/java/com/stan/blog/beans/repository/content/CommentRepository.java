package com.stan.blog.beans.repository.content;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stan.blog.beans.entity.content.CommentEntity;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    Page<CommentEntity> findByContentIdAndContentTypeAndDeletedFalse(String contentId, String contentType, Pageable pageable);

    Optional<CommentEntity> findByIdAndDeletedFalse(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE stan_blog_comment SET like_count = like_count + 1 WHERE id = :id AND deleted != true", nativeQuery = true)
    int incrementLikeCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE stan_blog_comment SET like_count = CASE WHEN like_count > 0 THEN like_count - 1 ELSE 0 END WHERE id = :id AND deleted != true", nativeQuery = true)
    int decrementLikeCount(@Param("id") Long id);
}
