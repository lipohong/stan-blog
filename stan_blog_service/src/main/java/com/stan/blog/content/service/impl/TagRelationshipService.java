package com.stan.blog.content.service.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stan.blog.beans.dto.content.TagRelationshipCreationDTO;
import com.stan.blog.beans.dto.content.TagRelationshipDTO;
import com.stan.blog.beans.entity.tag.TagInfoEntity;
import com.stan.blog.beans.entity.tag.TagRelationshipEntity;
import com.stan.blog.beans.repository.tag.TagInfoRepository;
import com.stan.blog.beans.repository.tag.TagRelationshipRepository;
import com.stan.blog.core.utils.BasicConverter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagRelationshipService {

    private final TagRelationshipRepository tagRelationshipRepository;
    private final TagInfoRepository tagInfoRepository;

    @Transactional
    public TagRelationshipDTO createTagRelationship(TagRelationshipCreationDTO dto) {
        TagRelationshipEntity entity = new TagRelationshipEntity();
        entity.setTagId(dto.getTagId());
        entity.setParentId(dto.getParentId());
        entity.setDeleted(Boolean.FALSE);
        entity.setCollectionId(dto.getCollectionId());
        TagRelationshipEntity saved = tagRelationshipRepository.save(entity);
        return BasicConverter.convert(saved, TagRelationshipDTO.class);
    }

    @Transactional(readOnly = true)
    public List<TagRelationshipDTO> getTagRelationshipByParentId(Long parentId, String collectionId) {
        List<TagRelationshipDTO> tree = buildTagTree(collectionId);
        if (parentId == null) {
            return tree;
        }
        TagRelationshipDTO parent = findNode(tree, parentId);
        return parent != null && parent.getChildren() != null ? parent.getChildren() : List.of();
    }

    @Transactional
    public void deleteById(Long id) {
        TagRelationshipEntity entity = tagRelationshipRepository.findById(id).orElse(null);
        if (entity == null) {
            return;
        }
        List<TagRelationshipEntity> relationships = tagRelationshipRepository.findByCollectionId(entity.getCollectionId());
        Set<Long> idsToDelete = collectSubtreeIds(id, relationships);
        if (idsToDelete.isEmpty()) {
            idsToDelete = Set.of(id);
        }
        tagRelationshipRepository.deleteAllById(idsToDelete);
    }

    @Transactional(readOnly = true)
    public List<TagRelationshipDTO> buildTagTree(String collectionId) {
        List<TagRelationshipEntity> relationships = tagRelationshipRepository.findByCollectionId(collectionId);
        if (relationships.isEmpty()) {
            return List.of();
        }
        Map<Long, TagInfoEntity> tagInfoMap = tagInfoRepository
                .findAllById(relationships.stream().map(TagRelationshipEntity::getTagId).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(TagInfoEntity::getId, entity -> entity));

        Map<Long, TagRelationshipDTO> dtoMap = new HashMap<>();
        relationships.forEach(rel -> {
            TagRelationshipDTO dto = new TagRelationshipDTO();
            dto.setId(rel.getId());
            dto.setParentId(rel.getParentId());
            dto.setTagId(rel.getTagId());
            dto.setCollectionId(rel.getCollectionId());
            TagInfoEntity tagInfo = tagInfoMap.get(rel.getTagId());
            dto.setLabel(tagInfo != null ? tagInfo.getKeyword() : null);
            dto.setChildren(new ArrayList<>());
            dtoMap.put(rel.getId(), dto);
        });

        List<TagRelationshipDTO> roots = new ArrayList<>();
        dtoMap.values().forEach(dto -> {
            Long parentId = dto.getParentId();
            if (parentId == null) {
                roots.add(dto);
            } else {
                TagRelationshipDTO parent = dtoMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(dto);
                } else {
                    roots.add(dto);
                }
            }
        });

        Comparator<TagRelationshipDTO> comparator = Comparator.comparing(
                TagRelationshipDTO::getLabel,
                Comparator.nullsLast(String::compareToIgnoreCase));
        roots.sort(comparator);
        roots.forEach(node -> sortChildren(node, comparator));
        return roots;
    }

    private Set<Long> collectSubtreeIds(Long rootId, List<TagRelationshipEntity> relationships) {
        Map<Long, List<TagRelationshipEntity>> childrenMap = relationships.stream()
                .filter(rel -> rel.getParentId() != null)
                .collect(Collectors.groupingBy(TagRelationshipEntity::getParentId));
        Deque<Long> stack = new ArrayDeque<>();
        Set<Long> ids = new HashSet<>();
        stack.push(rootId);
        while (!stack.isEmpty()) {
            Long current = stack.pop();
            ids.add(current);
            List<TagRelationshipEntity> children = childrenMap.get(current);
            if (children != null) {
                for (TagRelationshipEntity child : children) {
                    stack.push(child.getId());
                }
            }
        }
        return ids;
    }

    private void sortChildren(TagRelationshipDTO node, Comparator<TagRelationshipDTO> comparator) {
        if (node.getChildren() == null || node.getChildren().isEmpty()) {
            return;
        }
        node.getChildren().sort(comparator);
        node.getChildren().forEach(child -> sortChildren(child, comparator));
    }

    private TagRelationshipDTO findNode(List<TagRelationshipDTO> nodes, Long id) {
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }
        for (TagRelationshipDTO node : nodes) {
            if (Objects.equals(node.getId(), id)) {
                return node;
            }
            TagRelationshipDTO match = findNode(node.getChildren(), id);
            if (match != null) {
                return match;
            }
        }
        return null;
    }
}