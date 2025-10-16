package com.stan.blog.beans.repository.content;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.stan.blog.beans.entity.content.WordEntity;

@Repository
public interface WordRepository extends JpaRepository<WordEntity, Long> {
    @Query(value = "SELECT * FROM stan_blog_word_info WHERE vocabulary_id = :vocabularyId AND deleted != true", nativeQuery = true)
    List<WordEntity> findByVocabularyId(String vocabularyId);
}