package com.stan.blog.content.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.stan.blog.beans.dto.content.WordDTO;
import com.stan.blog.content.controller.impl.VOCController;
import com.stan.blog.content.service.impl.VOCService;
import com.stan.blog.content.service.impl.WordService;

class VOCControllerTests {

    private MockMvc mockMvc;
    private VOCService vocService;
    private WordService wordService;

    @BeforeEach
    void setUp() {
        vocService = org.mockito.Mockito.mock(VOCService.class);
        wordService = org.mockito.Mockito.mock(WordService.class);
        VOCController controller = new VOCController(vocService, wordService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getWordsByVocabularyDelegatesToService() throws Exception {
        WordDTO dto = new WordDTO();
        dto.setId(1L);
        dto.setVocabularyId("voc-1");
        when(wordService.getWordsByVOCId("voc-1")).thenReturn(List.of(dto));

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/vocabularies/{id}/words", "voc-1").param("id", "voc-1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(1));

        verify(wordService).getWordsByVOCId("voc-1");
    }
}

