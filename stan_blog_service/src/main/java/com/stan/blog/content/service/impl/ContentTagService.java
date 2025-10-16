package com.stan.blog.content.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stan.blog.beans.dto.tag.TagInfoDTO;
import com.stan.blog.beans.entity.content.ContentTagEntity;
import com.stan.blog.beans.entity.tag.TagInfoEntity;
import com.stan.blog.beans.repository.content.ContentTagRepository;
import com.stan.blog.beans.repository.tag.TagInfoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentTagService {

    private final ContentTagRepository contentTagRepository;
    private final TagInfoRepository tagInfoRepository;

    @Transactional
    public void replaceContentTags(String contentId, List<TagInfoDTO> tags) {
        contentTagRepository.deleteByContentId(contentId);
        if (tags == null || tags.isEmpty()) {
            return;
        }
        List<ContentTagEntity> entities = new ArrayList<>(tags.size());
        for (TagInfoDTO tag : tags) {
            entities.add(new ContentTagEntity(null, contentId, tag.getValue()));
        }
        contentTagRepository.saveAll(entities);
    }

    @Transactional(readOnly = true)
    public List<TagInfoDTO> findTagsForContent(String contentId) {
        List<ContentTagEntity> tagLinks = contentTagRepository.findByContentId(contentId);
        if (tagLinks.isEmpty()) {
            return List.of();
        }
        Set<Long> tagIds = tagLinks.stream()
                .map(ContentTagEntity::getTagId)
                .collect(Collectors.toSet());
        Map<Long, TagInfoEntity> tagInfoMap = tagInfoRepository.findAllById(tagIds).stream()
                .collect(Collectors.toMap(TagInfoEntity::getId, Function.identity()));
        return tagLinks.stream()
                .map(link -> tagInfoMap.get(link.getTagId()))
                .filter(info -> info != null)
                .map(TagInfoEntity::covertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, List<TagInfoDTO>> findTagsForContents(Collection<String> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            return Map.of();
        }
        List<ContentTagEntity> tagLinks = contentTagRepository.findByContentIdIn(contentIds);
        if (tagLinks.isEmpty()) {
            return Map.of();
        }
        Set<Long> tagIds = tagLinks.stream()
                .map(ContentTagEntity::getTagId)
                .collect(Collectors.toSet());
        Map<Long, TagInfoEntity> tagInfoMap = tagInfoRepository.findAllById(tagIds).stream()
                .collect(Collectors.toMap(TagInfoEntity::getId, Function.identity()));
        return tagLinks.stream()
                .collect(Collectors.groupingBy(ContentTagEntity::getContentId,
                        Collectors.mapping(link -> tagInfoMap.get(link.getTagId()), Collectors.toList())))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .filter(info -> info != null)
                                .map(TagInfoEntity::covertToDTO)
                                .toList()));
    }

    @Transactional(readOnly = true)
    public Set<String> findContentIdsWithTags(Collection<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return Set.of();
        }
        return contentTagRepository.findByTagIdIn(tagIds).stream()
                .map(ContentTagEntity::getContentId)
                .collect(Collectors.toSet());
    }
}