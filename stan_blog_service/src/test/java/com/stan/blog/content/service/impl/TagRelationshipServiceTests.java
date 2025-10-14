package com.stan.blog.content.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stan.blog.beans.dto.content.TagRelationshipCreationDTO;
import com.stan.blog.beans.dto.content.TagRelationshipDTO;
import com.stan.blog.beans.entity.tag.TagRelationshipEntity;

@ExtendWith(MockitoExtension.class)
class TagRelationshipServiceTests {

    @Spy
    @InjectMocks
    private TagRelationshipService tagRelationshipService;

    @Test
    void createTagRelationshipPersistsEntityAndReturnsDto() {
        TagRelationshipCreationDTO dto = new TagRelationshipCreationDTO();
        dto.setTagId(12L);
        dto.setParentId(3L);
        dto.setCollectionId("collection-1");

        doReturn(true).when(tagRelationshipService).save(any(TagRelationshipEntity.class));

        TagRelationshipDTO result = tagRelationshipService.createTagRelationship(dto);

        verify(tagRelationshipService).save(any(TagRelationshipEntity.class));
        assertNotNull(result);
        assertEquals(12L, result.getTagId());
        assertEquals(3L, result.getParentId());
        assertEquals("collection-1", result.getCollectionId());
    }

    @Test
    void deleteByIdRemovesSingleNodeWhenNoChildren() {
        TagRelationshipEntity entity = new TagRelationshipEntity();
        entity.setId(21L);
        entity.setCollectionId("collection-1");

        doReturn(entity).when(tagRelationshipService).getById(21L);
        doReturn(List.of()).when(tagRelationshipService).getTagRelationshipByParentId(21L, "collection-1");
        doReturn(true).when(tagRelationshipService).removeById(21L);

        tagRelationshipService.deleteById(21L);

        verify(tagRelationshipService).removeById(21L);
    }

    @Test
    void deleteByIdRemovesTreeWhenChildrenExist() {
        TagRelationshipEntity entity = new TagRelationshipEntity();
        entity.setId(30L);
        entity.setCollectionId("collection-2");

        TagRelationshipDTO child = new TagRelationshipDTO();
        child.setId(31L);
        TagRelationshipDTO grandChild = new TagRelationshipDTO();
        grandChild.setId(32L);
        child.setChildren(List.of(grandChild));

        doReturn(entity).when(tagRelationshipService).getById(30L);
        doReturn(List.of(child)).when(tagRelationshipService).getTagRelationshipByParentId(30L, "collection-2");
        doReturn(true).when(tagRelationshipService).removeBatchByIds(any());

        tagRelationshipService.deleteById(30L);

        ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
        verify(tagRelationshipService).removeBatchByIds(captor.capture());
        List<Long> ids = captor.getValue();
        assertEquals(3, ids.size());
        assertTrue(ids.containsAll(List.of(30L, 31L, 32L)));
    }
}
