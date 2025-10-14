package com.stan.blog.core.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stan.blog.beans.dto.tag.TagInfoCreationDTO;
import com.stan.blog.beans.dto.tag.TagInfoDTO;
import com.stan.blog.core.service.TagInfoService;

class TagInfoControllerTests {

    private MockMvc mockMvc;
    private TagInfoService tagInfoService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tagInfoService = org.mockito.Mockito.mock(TagInfoService.class);
        TagInfoController controller = new TagInfoController(tagInfoService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createTagDelegatesToService() throws Exception {
        TagInfoDTO dto = new TagInfoDTO();
        dto.setValue(1L);
        dto.setLabel("Java");
        when(tagInfoService.createTag(any(TagInfoCreationDTO.class))).thenReturn(dto);

        TagInfoCreationDTO request = new TagInfoCreationDTO();
        request.setKeyword("Java");

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.value").value(1));

        verify(tagInfoService).createTag(any(TagInfoCreationDTO.class));
    }

    @Test
    void getTagsByKeywordReturnsPage() throws Exception {
        Page<TagInfoDTO> page = new Page<>(1, 10);
        TagInfoDTO dto = new TagInfoDTO();
        dto.setValue(2L);
        dto.setLabel("Spring");
        page.setRecords(java.util.List.of(dto));
        when(tagInfoService.getTagsByKeyword("java", 1, 10)).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/tags")
                .param("keyword", "java"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.records[0].value").value(2));

        verify(tagInfoService).getTagsByKeyword(eq("java"), eq(1), eq(10));
    }
}
