package com.stan.blog.content.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stan.blog.beans.dto.content.TagRelationshipCreationDTO;
import com.stan.blog.beans.dto.content.TagRelationshipDTO;
import com.stan.blog.beans.entity.tag.TagRelationshipEntity;
import com.stan.blog.beans.repository.tag.TagRelationshipRepository;
import com.stan.blog.beans.repository.tag.TagInfoRepository;

@ExtendWith(MockitoExtension.class)
class TagRelationshipServiceTests {

    @Mock
    private TagRelationshipRepository tagRelationshipRepository;

    @Mock
    private TagInfoRepository tagInfoRepository;

    @InjectMocks
    private TagRelationshipService tagRelationshipService;

    @Test
    void createTagRelationshipPersistsEntityAndReturnsDto() {
        TagRelationshipCreationDTO dto = new TagRelationshipCreationDTO();
        dto.setTagId(12L);
        dto.setParentId(3L);
        dto.setCollectionId("collection-1");

        TagRelationshipEntity savedEntity = new TagRelationshipEntity();
        savedEntity.setTagId(12L);
        savedEntity.setParentId(3L);
        savedEntity.setCollectionId("collection-1");
        when(tagRelationshipRepository.save(any(TagRelationshipEntity.class))).thenReturn(savedEntity);

        TagRelationshipDTO result = tagRelationshipService.createTagRelationship(dto);

        verify(tagRelationshipRepository).save(any(TagRelationshipEntity.class));
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

        when(tagRelationshipRepository.findById(21L)).thenReturn(Optional.of(entity));
        when(tagRelationshipRepository.findByCollectionId("collection-1")).thenReturn(List.of());

        tagRelationshipService.deleteById(21L);

        ArgumentCaptor<Iterable<Long>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(tagRelationshipRepository).deleteAllById(captor.capture());
        List<Long> ids = StreamSupport.stream(captor.getValue().spliterator(), false).collect(Collectors.toList());
        assertEquals(1, ids.size());
        assertTrue(ids.contains(21L));
    }

    @Test
    void deleteByIdRemovesTreeWhenChildrenExist() {
        TagRelationshipEntity entity = new TagRelationshipEntity();
        entity.setId(30L);
        entity.setCollectionId("collection-2");

        TagRelationshipEntity childRel = new TagRelationshipEntity();
        childRel.setId(31L);
        childRel.setParentId(30L);
        TagRelationshipEntity grandChildRel = new TagRelationshipEntity();
        grandChildRel.setId(32L);
        grandChildRel.setParentId(31L);

        when(tagRelationshipRepository.findById(30L)).thenReturn(Optional.of(entity));
        when(tagRelationshipRepository.findByCollectionId("collection-2")).thenReturn(List.of(entity, childRel, grandChildRel));

        tagRelationshipService.deleteById(30L);

        ArgumentCaptor<Iterable<Long>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(tagRelationshipRepository).deleteAllById(captor.capture());
        List<Long> ids = StreamSupport.stream(captor.getValue().spliterator(), false).collect(Collectors.toList());
        assertEquals(3, ids.size());
        assertTrue(ids.containsAll(List.of(30L, 31L, 32L)));
    }
}
