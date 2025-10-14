package com.stan.blog.content.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stan.blog.beans.dto.content.PlanDTO;
import com.stan.blog.beans.dto.content.PlanProgressDTO;
import com.stan.blog.content.controller.impl.PlanController;
import com.stan.blog.content.service.impl.PlanProgressService;
import com.stan.blog.content.service.impl.PlanService;

class PlanControllerTests {

    private MockMvc mockMvc;
    private PlanService planService;
    private PlanProgressService progressService;

    @BeforeEach
    void setUp() {
        planService = org.mockito.Mockito.mock(PlanService.class);
        progressService = org.mockito.Mockito.mock(PlanProgressService.class);
        PlanController controller = new PlanController(planService, progressService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getPlanProgressesDelegatesToService() throws Exception {
        Page<PlanProgressDTO> page = new Page<>(1, 10);
        when(progressService.getProgressesByPlanId("plan-1", 1, 10)).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/plans/{id}/progresses", "plan-1"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(progressService).getProgressesByPlanId("plan-1", 1, 10);
    }

    @Test
    void getPlanByIdUsesService() throws Exception {
        PlanDTO dto = new PlanDTO();
        dto.setId("plan-2");
        when(planService.getDTOById("plan-2")).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/plans/{id}", "plan-2"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("plan-2"));

        verify(planService).getDTOById("plan-2");
    }
}
