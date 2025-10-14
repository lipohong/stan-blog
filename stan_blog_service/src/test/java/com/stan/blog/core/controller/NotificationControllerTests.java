package com.stan.blog.core.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stan.blog.DefaultTestData;
import com.stan.blog.beans.dto.content.NotificationDTO;
import com.stan.blog.core.service.NotificationService;
import com.stan.blog.core.utils.SecurityUtil;

class NotificationControllerTests {

    private NotificationService notificationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        notificationService = org.mockito.Mockito.mock(NotificationService.class);
        NotificationController controller = new NotificationController(notificationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        SecurityUtil.setUserDetail(DefaultTestData.getDefaultUserAuthentication());
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getNotificationsReturnsPageForAuthenticatedUser() throws Exception {
        Page<NotificationDTO> page = new Page<>(1, 10);
        NotificationDTO dto = new NotificationDTO();
        dto.setId(1L);
        page.setRecords(java.util.List.of(dto));
        when(notificationService.getUserNotifications(1L, 1, 10, null)).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/notifications")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.records[0].id").value(1));

        verify(notificationService).getUserNotifications(1L, 1, 10, null);
    }

    @Test
    void getNotificationsReturnsUnauthorizedWhenNoUser() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/notifications"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        verify(notificationService, never()).getUserNotifications(anyLong(), anyInt(), anyInt(), any());
    }

    @Test
    void markAsReadReturnsOkWhenServiceSucceeds() throws Exception {
        when(notificationService.markAsRead(5L, 1L)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.put("/v1/notifications/{id}/read", 5))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(notificationService).markAsRead(5L, 1L);
    }

    @Test
    void markAsReadReturnsNotFoundWhenServiceFails() throws Exception {
        when(notificationService.markAsRead(7L, 1L)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.put("/v1/notifications/{id}/read", 7))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void markAllAsReadReturnsCount() throws Exception {
        when(notificationService.markAllAsRead(1L)).thenReturn(3);

        mockMvc.perform(MockMvcRequestBuilders.put("/v1/notifications/mark-all-read"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("3"));

        verify(notificationService).markAllAsRead(1L);
    }

    @Test
    void unreadCountReturnsValue() throws Exception {
        when(notificationService.getCurrentUserUnreadCount()).thenReturn(4L);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/notifications/unread-count"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("4"));

        verify(notificationService).getCurrentUserUnreadCount();
    }
}


