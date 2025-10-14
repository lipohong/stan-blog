package com.stan.blog.content.controller;

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
import com.stan.blog.beans.dto.content.PlanProgressCreationDTO;
import com.stan.blog.beans.dto.content.PlanProgressDTO;
import com.stan.blog.beans.dto.content.PlanProgressUpdateDTO;
import com.stan.blog.content.controller.impl.PlanProgressController;
import com.stan.blog.content.service.impl.PlanProgressService;

class PlanProgressControllerTests {

    private MockMvc mockMvc;
    private PlanProgressService progressService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        progressService = org.mockito.Mockito.mock(PlanProgressService.class);
        PlanProgressController controller = new PlanProgressController(progressService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getProgressByPlanIdReturnsPage() throws Exception {
        Page<PlanProgressDTO> page = new Page<>(1, 10);
        when(progressService.getProgressesByPlanId("plan-1", 1, 10)).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/plan-progresses")
                .param("planId", "plan-1"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(progressService).getProgressesByPlanId("plan-1", 1, 10);
    }

    @Test
    void createProgressDelegatesToService() throws Exception {
        PlanProgressDTO dto = new PlanProgressDTO();
        dto.setId("pp-1");
        when(progressService.saveProgress(any(PlanProgressCreationDTO.class))).thenReturn(dto);

        PlanProgressCreationDTO request = new PlanProgressCreationDTO();
        request.setPlanId("plan-1");

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/plan-progresses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("pp-1"));

        verify(progressService).saveProgress(any(PlanProgressCreationDTO.class));
    }

    @Test
    void updateProgressDelegatesToService() throws Exception {
        PlanProgressDTO dto = new PlanProgressDTO();
        dto.setId("pp-2");
        when(progressService.updateProgress(any(PlanProgressUpdateDTO.class))).thenReturn(dto);

        PlanProgressUpdateDTO request = new PlanProgressUpdateDTO();
        request.setId("pp-2");

        mockMvc.perform(MockMvcRequestBuilders.put("/v1/plan-progresses/{id}", "pp-2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("pp-2"));

        verify(progressService).updateProgress(any(PlanProgressUpdateDTO.class));
    }

    @Test
    void deleteProgressDelegatesToService() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/plan-progresses/{id}", 20))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(progressService).removeById(20L);
    }
}
