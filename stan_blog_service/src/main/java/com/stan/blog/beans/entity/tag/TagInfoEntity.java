package com.stan.blog.beans.entity.tag;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.stan.blog.beans.dto.tag.TagInfoDTO;
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
@Table(name = "stan_blog_tag_info")
@SQLDelete(sql = "UPDATE tag_info SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class TagInfoEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String keyword;

    public static TagInfoDTO covertToDTO(TagInfoEntity entity) {
        return TagInfoDTO.builder().value(entity.id).label(entity.keyword).build();
    }
}
