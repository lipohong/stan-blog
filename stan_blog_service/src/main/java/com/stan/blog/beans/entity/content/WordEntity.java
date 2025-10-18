package com.stan.blog.beans.entity.content;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.stan.blog.beans.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "stan_blog_word_info")
@SQLDelete(sql = "UPDATE stan_blog_word_info SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class WordEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String vocabularyId;
    private String text;
    private String meaningInChinese;
    private String meaningInEnglish;
    private String partOfSpeech;
}
