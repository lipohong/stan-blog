package com.stan.blog.content.service.impl;

import org.springframework.stereotype.Service;

import com.stan.blog.beans.consts.Const.ContentType;
import com.stan.blog.beans.dto.content.VOCCreationDTO;
import com.stan.blog.beans.dto.content.VOCDTO;
import com.stan.blog.beans.dto.content.VOCUpdateDTO;
import com.stan.blog.beans.entity.content.VOCEntity;
import com.stan.blog.content.mapper.VOCMapper;

@Service
public class VOCService extends BaseContentService<VOCDTO, VOCCreationDTO, VOCUpdateDTO, VOCEntity, VOCMapper> {

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
