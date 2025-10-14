package com.stan.blog.content.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stan.blog.beans.entity.content.ContentTagEntity;

@SpringBootTest
@ActiveProfiles("test")
class ContentTagServiceIntegrationTests {

    @Autowired
    private ContentTagService contentTagService;

    @Test
    void saveAndRetrieveContentTags() {
        String contentId = "ct-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        ContentTagEntity entity = new ContentTagEntity(contentId, 999L);

        assertTrue(contentTagService.save(entity));

        List<ContentTagEntity> stored = contentTagService.list(
                new LambdaQueryWrapper<ContentTagEntity>().eq(ContentTagEntity::getContentId, contentId));

        assertEquals(1, stored.size());
        assertEquals(999L, stored.get(0).getTagId());
    }
}

