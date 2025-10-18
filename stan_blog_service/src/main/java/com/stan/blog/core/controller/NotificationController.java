package com.stan.blog.core.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stan.blog.beans.dto.content.NotificationDTO;
import com.stan.blog.core.dto.PageResponse;
import com.stan.blog.core.service.NotificationService;
import com.stan.blog.core.utils.AuthenticationUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<PageResponse<NotificationDTO>> getCurrentUserNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean isRead) {

        return AuthenticationUtil.withAuthenticatedUserId(userId -> {
            PageResponse<NotificationDTO> notifications = PageResponse.from(
                    notificationService.getUserNotifications(userId, page, size, isRead));
            return ResponseEntity.ok(notifications);
        });
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getCurrentUserUnreadCount() {
        long count = notificationService.getCurrentUserUnreadCount();
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        return AuthenticationUtil.withAuthenticatedUserId(userId -> {
            boolean success = notificationService.markAsRead(notificationId, userId);

            if (success) {
                return ResponseEntity.ok().<Void>build();
            } else {
                return ResponseEntity.notFound().build();
            }
        });
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<Integer> markAllAsRead() {
        return AuthenticationUtil.withAuthenticatedUserId(userId -> {
            int count = notificationService.markAllAsRead(userId);
            return ResponseEntity.ok(count);
        });
    }
}