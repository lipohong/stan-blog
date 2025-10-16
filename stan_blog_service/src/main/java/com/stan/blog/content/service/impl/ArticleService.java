package com.stan.blog.content.service.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.stan.blog.beans.consts.Const.ContentType;
import com.stan.blog.beans.dto.content.ArticleCreationDTO;
import com.stan.blog.beans.dto.content.ArticleDTO;
import com.stan.blog.beans.dto.content.ArticleUpdateDTO;
import com.stan.blog.beans.entity.content.ArticleEntity;
import com.stan.blog.beans.repository.content.ArticleRepository;
import com.stan.blog.beans.repository.user.UserRepository;
import com.stan.blog.core.utils.CacheUtil;

@Service
public class ArticleService extends
        BaseContentService<ArticleDTO, ArticleCreationDTO, ArticleUpdateDTO, ArticleEntity> {

    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository,
                          ContentGeneralInfoService contentGeneralInfoService,
                          ContentAdminService contentAdminService,
                          ContentTagService contentTagService,
                          UserRepository userRepository,
                          CacheUtil cacheUtil) {
        super(contentGeneralInfoService, contentAdminService, contentTagService, userRepository, cacheUtil);
        this.articleRepository = articleRepository;
    }

    @Override
    protected JpaRepository<ArticleEntity, String> getRepository() {
        return articleRepository;
    }

    @Override
    protected ArticleDTO getConcreteDTO() {
        return new ArticleDTO();
    }

    @Override
    protected ArticleEntity getConcreteEntity() {
        return new ArticleEntity();
    }

    @Override
    protected ContentType getContentType() {
        return ContentType.ARTICLE;
    }
}