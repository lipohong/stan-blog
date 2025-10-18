package com.stan.blog.beans.repository.content;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stan.blog.beans.entity.content.WordEntity;

@Repository
public interface WordRepository extends JpaRepository<WordEntity, Long> {
    List<WordEntity> findByVocabularyId(String vocabularyId);
}