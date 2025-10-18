package com.stan.blog.beans.repository.tag;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stan.blog.beans.entity.tag.TagRelationshipEntity;

@Repository
public interface TagRelationshipRepository extends JpaRepository<TagRelationshipEntity, Long> {

    List<TagRelationshipEntity> findByCollectionIdAndParentId(String collectionId, Long parentId);

    List<TagRelationshipEntity> findByCollectionId(String collectionId);
}