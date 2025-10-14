package com.stan.blog.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.stan.blog.DefaultTestData;
import com.stan.blog.beans.consts.Const.NotificationType;
import com.stan.blog.beans.dto.content.NotificationCreateDTO;
import com.stan.blog.beans.dto.content.NotificationDTO;
import com.stan.blog.beans.entity.content.NotificationEntity;
import com.stan.blog.beans.entity.user.UserEntity;
import com.stan.blog.core.mapper.NotificationMapper;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTests {

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private UserService userService;

    @Spy
    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationService, "baseMapper", notificationMapper);
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

        UserEntity sender = new UserEntity();
        sender.setFirstName("Alice");
        sender.setLastName("Smith");

        doReturn(true).when(notificationService).save(any(NotificationEntity.class));
        when(userService.getById(5L)).thenReturn(sender);

        NotificationDTO result = notificationService.createNotification(dto);

        verify(notificationService).save(any(NotificationEntity.class));
        assertEquals(NotificationType.CONTENT_RECOMMENDED, result.getNotificationType());
        assertEquals("Alice Smith", result.getSenderName());
    }

    @Test
    void notifyContentCommentedSkipsWhenOwnerIsCommenter() {
        org.mockito.Mockito.lenient().doReturn(null).when(notificationService).createNotification(any());

        notificationService.notifyContentCommented("c1", "Title", "ARTICLE", 3L, 3L, "Owner", "Nice!");

        verify(notificationService, never()).createNotification(any());
    }

    @Test
    void getCurrentUserUnreadCountReturnsZeroWhenNoAuthentication() {
        long count = notificationService.getCurrentUserUnreadCount();
        assertEquals(0L, count);
    }

    @Test
    void getCurrentUserUnreadCountDelegatesToMapper() {
        com.stan.blog.core.utils.SecurityUtil.setUserDetail(DefaultTestData.getDefaultUserAuthentication());
        when(notificationMapper.getUnreadCount(DefaultTestData.DefaultUser.USER_ID)).thenReturn(4L);

        long count = notificationService.getCurrentUserUnreadCount();

        assertEquals(4L, count);
        verify(notificationMapper).getUnreadCount(DefaultTestData.DefaultUser.USER_ID);
    }
}

