package com.stan.blog.content.service.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.stan.blog.beans.consts.Const.ContentType;
import com.stan.blog.beans.dto.content.VOCCreationDTO;
import com.stan.blog.beans.dto.content.VOCDTO;
import com.stan.blog.beans.dto.content.VOCUpdateDTO;
import com.stan.blog.beans.entity.content.VOCEntity;
import com.stan.blog.beans.repository.content.VOCRepository;
import com.stan.blog.beans.repository.user.UserRepository;
import com.stan.blog.core.utils.CacheUtil;

@Service
public class VOCService extends BaseContentService<VOCDTO, VOCCreationDTO, VOCUpdateDTO, VOCEntity> {

    private final VOCRepository vocRepository;

    public VOCService(VOCRepository vocRepository,
                      ContentGeneralInfoService contentGeneralInfoService,
                      ContentAdminService contentAdminService,
                      ContentTagService contentTagService,
                      UserRepository userRepository,
                      CacheUtil cacheUtil) {
        super(contentGeneralInfoService, contentAdminService, contentTagService, userRepository, cacheUtil);
        this.vocRepository = vocRepository;
    }

    @Override
    protected JpaRepository<VOCEntity, String> getRepository() {
        return vocRepository;
    }

    @Override
    protected VOCDTO getConcreteDTO() {
        return new VOCDTO();
    }

    @Override
    protected VOCEntity getConcreteEntity() {
        return new VOCEntity();
    }

    @Override
    protected ContentType getContentType() {
        return ContentType.VOCABULARY;
    }
}