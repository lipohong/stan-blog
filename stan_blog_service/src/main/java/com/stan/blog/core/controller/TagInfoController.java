package com.stan.blog.core.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stan.blog.beans.dto.tag.TagInfoCreationDTO;
import com.stan.blog.beans.dto.tag.TagInfoDTO;
import com.stan.blog.core.dto.PageResponse;
import com.stan.blog.core.service.TagInfoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/tags")
@RequiredArgsConstructor
public class TagInfoController {

    private final TagInfoService tagInfoService;

    @PostMapping
    public ResponseEntity<TagInfoDTO> createTag(@RequestBody TagInfoCreationDTO dto) {
        return ResponseEntity.ok(tagInfoService.createTag(dto));
    }

    @GetMapping
    public ResponseEntity<PageResponse<TagInfoDTO>> getTagsByKeyword(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "current", required = false, defaultValue = "1") int current,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return ResponseEntity.ok(PageResponse.from(tagInfoService.getTagsByKeyword(keyword, current, size)));
    }
}