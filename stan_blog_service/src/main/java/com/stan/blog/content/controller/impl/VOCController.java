package com.stan.blog.content.controller.impl;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stan.blog.beans.dto.content.VOCCreationDTO;
import com.stan.blog.beans.dto.content.VOCDTO;
import com.stan.blog.beans.dto.content.VOCUpdateDTO;
import com.stan.blog.beans.dto.content.WordDTO;
import com.stan.blog.beans.entity.content.VOCEntity;
import com.stan.blog.content.service.impl.VOCService;
import com.stan.blog.content.service.impl.WordService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/vocabularies")
@RequiredArgsConstructor
public class VOCController
        extends BaseContentController<VOCDTO, VOCCreationDTO, VOCUpdateDTO, VOCEntity, VOCService> {

    private final VOCService vocService;
    private final WordService wordService;

    @GetMapping("/{id}/words")
    public ResponseEntity<List<WordDTO>> getWordsByVOCId(@PathVariable String id) {
        return ResponseEntity.ok(wordService.getWordsByVOCId(id));
    }

    @Override
    protected VOCService getConcreteSubContentService() {
        return this.vocService;
    }
}