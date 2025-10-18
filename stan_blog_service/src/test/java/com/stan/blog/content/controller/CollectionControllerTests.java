package com.stan.blog.content.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.stan.blog.beans.dto.content.CollectionDTO;
import com.stan.blog.content.controller.impl.CollectionController;
import com.stan.blog.content.service.impl.CollectionService;

class CollectionControllerTests {

    private MockMvc mockMvc;
    private CollectionService collectionService;

    @BeforeEach
    void setUp() {
        collectionService = org.mockito.Mockito.mock(CollectionService.class);
        CollectionController controller = new CollectionController(collectionService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getCollectionPreviewDelegatesToService() throws Exception {
        CollectionDTO dto = new CollectionDTO();
        dto.setId("col-1");
        when(collectionService.getDTOByIdWithRelatedContents("col-1")).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/collections/{id}/preview", "col-1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("col-1"));

        verify(collectionService).getDTOByIdWithRelatedContents("col-1");
    }
}
