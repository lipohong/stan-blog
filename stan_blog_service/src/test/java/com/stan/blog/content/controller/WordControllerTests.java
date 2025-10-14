package com.stan.blog.content.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stan.blog.beans.dto.content.WordCreationDTO;
import com.stan.blog.beans.dto.content.WordDTO;
import com.stan.blog.beans.dto.content.WordUpdateDTO;
import com.stan.blog.content.controller.impl.WordController;
import com.stan.blog.content.service.impl.WordService;

class WordControllerTests {

    private MockMvc mockMvc;
    private WordService wordService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        wordService = org.mockito.Mockito.mock(WordService.class);
        WordController controller = new WordController(wordService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getWordsByVocabularyDelegatesToService() throws Exception {
        WordDTO dto = new WordDTO();
        dto.setId(1L);
        dto.setVocabularyId("voc-1");
        when(wordService.getWordsByVOCId("voc-1")).thenReturn(List.of(dto));

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/words")
                .param("vocabularyId", "voc-1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(1));

        verify(wordService).getWordsByVOCId("voc-1");
    }

    @Test
    void createWordReturnsSavedDto() throws Exception {
        WordDTO dto = new WordDTO();
        dto.setId(2L);
        when(wordService.saveWord(any(WordCreationDTO.class))).thenReturn(dto);

        WordCreationDTO request = new WordCreationDTO();
        request.setVocabularyId("voc-1");
        request.setText("Hello");

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/words")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(2));

        verify(wordService).saveWord(any(WordCreationDTO.class));
    }

    @Test
    void updateWordDelegatesToService() throws Exception {
        WordDTO dto = new WordDTO();
        dto.setId(3L);
        when(wordService.updateWord(any(WordUpdateDTO.class))).thenReturn(dto);

        WordUpdateDTO request = new WordUpdateDTO();
        request.setId(3L);
        request.setText("Updated");

        mockMvc.perform(MockMvcRequestBuilders.put("/v1/words/{id}", 3)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(3));

        verify(wordService).updateWord(any(WordUpdateDTO.class));
    }

    @Test
    void getWordByIdDelegatesToService() throws Exception {
        WordDTO dto = new WordDTO();
        dto.setId(4L);
        when(wordService.getWordById(4L)).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/words/{id}", 4))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(4));

        verify(wordService).getWordById(4L);
    }

    @Test
    void deleteWordDelegatesToService() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/words/{id}", 5))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(wordService).removeById(5L);
    }
}
