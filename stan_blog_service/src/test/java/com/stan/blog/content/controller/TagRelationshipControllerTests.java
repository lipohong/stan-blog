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
import com.stan.blog.beans.dto.content.TagRelationshipCreationDTO;
import com.stan.blog.beans.dto.content.TagRelationshipDTO;
import com.stan.blog.content.controller.impl.TagRelationshipController;
import com.stan.blog.content.service.impl.TagRelationshipService;

class TagRelationshipControllerTests {

    private MockMvc mockMvc;
    private TagRelationshipService tagRelationshipService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tagRelationshipService = org.mockito.Mockito.mock(TagRelationshipService.class);
        TagRelationshipController controller = new TagRelationshipController(tagRelationshipService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createTagRelationshipDelegatesToService() throws Exception {
        TagRelationshipDTO dto = new TagRelationshipDTO();
        dto.setId(1L);
        when(tagRelationshipService.createTagRelationship(any(TagRelationshipCreationDTO.class))).thenReturn(dto);

        TagRelationshipCreationDTO request = new TagRelationshipCreationDTO();
        request.setCollectionId("col-1");
        request.setTagId(2L);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/tag-relationships")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1));

        verify(tagRelationshipService).createTagRelationship(any(TagRelationshipCreationDTO.class));
    }

    @Test
    void deleteTagRelationshipDelegatesToService() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/tag-relationships/{id}", 7))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(tagRelationshipService).deleteById(7L);
    }

    @Test
    void getTagRelationshipsByParentIdDelegatesToService() throws Exception {
        when(tagRelationshipService.getTagRelationshipByParentId(1L, "col-1")).thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/tag-relationships")
                .param("parentId", "1")
                .param("collectionId", "col-1"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(tagRelationshipService).getTagRelationshipByParentId(1L, "col-1");
    }
}