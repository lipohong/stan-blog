package com.stan.blog.content.service.impl;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.stan.blog.beans.consts.Const.ContentType;
import com.stan.blog.beans.dto.content.CollectionCreationDTO;
import com.stan.blog.beans.dto.content.CollectionDTO;
import com.stan.blog.beans.dto.content.CollectionUpdateDTO;
import com.stan.blog.beans.entity.content.CollectionEntity;
import com.stan.blog.beans.repository.content.CollectionRepository;
import com.stan.blog.beans.repository.user.UserRepository;
import com.stan.blog.core.utils.CacheUtil;
import com.stan.blog.core.utils.TagRelationshipUtil;

@Service
public class CollectionService extends BaseContentService<CollectionDTO, CollectionCreationDTO, CollectionUpdateDTO, CollectionEntity> {

    private final CollectionRepository collectionRepository;
    private final TagRelationshipService tagRelationshipService;
    private final TagRelationshipUtil tagRelationshipUtil;

    public CollectionService(CollectionRepository collectionRepository,
                             TagRelationshipService tagRelationshipService,
                             TagRelationshipUtil tagRelationshipUtil,
                             ContentGeneralInfoService contentGeneralInfoService,
                             ContentAdminService contentAdminService,
                             ContentTagService contentTagService,
                             UserRepository userRepository,
                             CacheUtil cacheUtil) {
        super(contentGeneralInfoService, contentAdminService, contentTagService, userRepository, cacheUtil);
        this.collectionRepository = collectionRepository;
        this.tagRelationshipService = tagRelationshipService;
        this.tagRelationshipUtil = tagRelationshipUtil;
    }

    @Override
    protected JpaRepository<CollectionEntity, String> getRepository() {
        return collectionRepository;
    }

    @Override
    public CollectionDTO getDTOById(String id) {
        CollectionDTO result = super.getDTOById(id);
        if (result != null) {
            List<com.stan.blog.beans.dto.content.TagRelationshipDTO> tagTree = tagRelationshipService.buildTagTree(id);
            result.setTagTree(tagTree);
        }
        return result;
    }

    public CollectionDTO getDTOByIdWithRelatedContents(String id) {
        CollectionDTO result = this.getDTOById(id);
        if (result != null && result.getTagTree() != null && !result.getTagTree().isEmpty()) {
            tagRelationshipUtil.setRelatedContentsForTagTree(result.getTagTree(), result.getOwnerId());
        }
        return result;
    }

    @Override
    protected CollectionDTO getConcreteDTO() {
        return new CollectionDTO();
    }

    @Override
    protected CollectionEntity getConcreteEntity() {
        return new CollectionEntity();
    }

    @Override
    protected ContentType getContentType() {
        return ContentType.COLLECTION;
    }
}