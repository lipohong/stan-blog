package com.stan.blog.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import com.stan.blog.DefaultTestData;
import com.stan.blog.beans.consts.Const.NotificationType;
import com.stan.blog.beans.dto.content.NotificationCreateDTO;
import com.stan.blog.beans.dto.content.NotificationDTO;
import com.stan.blog.beans.dto.user.UserGeneralDTO;
import com.stan.blog.beans.entity.content.NotificationEntity;
import com.stan.blog.beans.repository.content.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTests {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        // no-op
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createNotificationPersistsEntityAndReturnsDtoWithSenderName() {
        NotificationCreateDTO dto = new NotificationCreateDTO();
        dto.setRecipientId(9L);
        dto.setSenderId(5L);
        dto.setNotificationType(NotificationType.CONTENT_RECOMMENDED);
        dto.setTitle("title");
        dto.setMessage("message");

        NotificationEntity savedEntity = new NotificationEntity();
        savedEntity.setId(1L);
        savedEntity.setRecipientId(9L);
        savedEntity.setSenderId(5L);
        savedEntity.setNotificationType(NotificationType.CONTENT_RECOMMENDED);
        savedEntity.setTitle("title");
        savedEntity.setMessage("message");

        UserGeneralDTO sender = new UserGeneralDTO();
        sender.setFirstName("Alice");
        sender.setLastName("Smith");

        when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(savedEntity);
        when(userService.getUser(5L)).thenReturn(sender);

        NotificationDTO result = notificationService.createNotification(dto);

        verify(notificationRepository).save(any(NotificationEntity.class));
        assertEquals(NotificationType.CONTENT_RECOMMENDED, result.getNotificationType());
        assertEquals("Alice Smith", result.getSenderName());
    }

    @Test
    void notifyContentCommentedSkipsWhenOwnerIsCommenter() {
        notificationService.notifyContentCommented("c1", "Title", "ARTICLE", 3L, 3L, "Owner", "Nice!");
        verify(notificationRepository, never()).save(any(NotificationEntity.class));
    }

    @Test
    void getCurrentUserUnreadCountReturnsZeroWhenNoAuthentication() {
        long count = notificationService.getCurrentUserUnreadCount();
        assertEquals(0L, count);
    }

    @Test
    void getCurrentUserUnreadCountDelegatesToRepository() {
        com.stan.blog.core.utils.SecurityUtil.setUserDetail(DefaultTestData.getDefaultUserAuthentication());
        when(notificationRepository.countByRecipientIdAndDeletedFalseAndIsReadFalse(DefaultTestData.DefaultUser.USER_ID))
                .thenReturn(4L);

        long count = notificationService.getCurrentUserUnreadCount();

        assertEquals(4L, count);
        verify(notificationRepository).countByRecipientIdAndDeletedFalseAndIsReadFalse(DefaultTestData.DefaultUser.USER_ID);
    }
}

