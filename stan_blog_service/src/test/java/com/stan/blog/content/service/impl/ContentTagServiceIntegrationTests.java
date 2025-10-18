package com.stan.blog.content.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.stan.blog.beans.dto.tag.TagInfoDTO;
import com.stan.blog.beans.entity.tag.TagInfoEntity;
import com.stan.blog.beans.repository.tag.TagInfoRepository;

@SpringBootTest
@ActiveProfiles("test")
class ContentTagServiceIntegrationTests {

    @Autowired
    private ContentTagService contentTagService;

    @Autowired
    private TagInfoRepository tagInfoRepository;

    @Test
    void saveAndRetrieveContentTags() {
        String contentId = "ct-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 20);

        TagInfoEntity tag = new TagInfoEntity();
        tag.setKeyword("java-" + java.util.UUID.randomUUID().toString().substring(0, 8));
        tag = tagInfoRepository.save(tag);

        TagInfoDTO dto = new TagInfoDTO();
        dto.setValue(tag.getId());
        dto.setLabel(tag.getKeyword());

        contentTagService.replaceContentTags(contentId, List.of(dto));

        List<TagInfoDTO> stored = contentTagService.findTagsForContent(contentId);

        assertEquals(1, stored.size());
        assertEquals(tag.getId(), stored.get(0).getValue());
        assertEquals(tag.getKeyword(), stored.get(0).getLabel());
    }
}

