package com.stan.blog.content.service;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.stan.blog.beans.dto.content.CommentDTO;
import com.stan.blog.beans.dto.user.EnhancedUserDetail;
import com.stan.blog.beans.dto.user.UserGeneralDTO;
import com.stan.blog.beans.entity.content.CommentEntity;
import com.stan.blog.beans.repository.content.CommentRepository;
import com.stan.blog.content.service.impl.ContentGeneralInfoService;
import com.stan.blog.core.service.NotificationService;
import com.stan.blog.core.utils.BasicConverter;

/**
 * Tests for CommentService
 *
 * Note: User display name building tests have been moved to UserDisplayNameUtilTests
 * since the buildUserDisplayName method is now extracted to a utility class.
 *
 * Authentication validation tests have been moved to controller layer tests
 * since authentication is now handled in CommentController.
 *
 * Most business logic tests that require database operations are better tested
 * at the integration test level or controller test level.
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class CommentServiceTests {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ContentGeneralInfoService contentGeneralInfoService;

    @Mock
    private EnhancedUserDetail currentUser;

    @Mock
    private UserGeneralDTO userProfile;

    @InjectMocks
    private CommentService commentService;

    @Test
    @DisplayName("Comment Service: BasicConverter test for CommentEntity to CommentDTO")
    void testBasicConverter_CommentEntityToDTO() throws Exception {
        CommentEntity entity = new CommentEntity();
        entity.setId(123L);
        entity.setContentId("article-456");
        entity.setContentType("ARTICLE");
        entity.setContent("Test comment content");
        entity.setUserId(789L);
        entity.setUserName("John Doe");
        entity.setUserAvatarUrl("https://example.com/avatar.jpg");
        entity.setParentId(999L);
        entity.setLikeCount(5L);
        entity.setIpAddress("192.168.1.1");
        entity.setDeleted(false);

        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        entity.setCreateTime(currentTime);
        entity.setUpdateTime(currentTime);
        entity.setCreateBy("testuser");
        entity.setUpdateBy("testuser");

        CommentDTO dto = BasicConverter.convert(entity, CommentDTO.class);

        assertEquals(123L, dto.getId());
        assertEquals("article-456", dto.getContentId());
        assertEquals("ARTICLE", dto.getContentType());
        assertEquals("Test comment content", dto.getContent());
        assertEquals(789L, dto.getUserId());
        assertEquals("John Doe", dto.getUserName());
        assertEquals("https://example.com/avatar.jpg", dto.getUserAvatarUrl());
        assertEquals(999L, dto.getParentId());
        assertEquals(5L, dto.getLikeCount());
        assertEquals(false, dto.getDeleted());
        assertNotNull(dto.getCreateTime(), "CreateTime should not be null");
        assertNotNull(dto.getUpdateTime(), "UpdateTime should not be null");
        assertEquals(currentTime, dto.getCreateTime(), "CreateTime should match");
        assertEquals(currentTime, dto.getUpdateTime(), "UpdateTime should match");
    }

    @Test
    @DisplayName("Comment Service: convertToDTO method test")
    void testConvertToDTO() throws Exception {
        CommentEntity entity = new CommentEntity();
        entity.setId(456L);
        entity.setContentId("article-789");
        entity.setContentType("ARTICLE");
        entity.setContent("Test comment for convertToDTO");
        entity.setUserId(123L);
        entity.setUserName("Jane Doe");
        entity.setUserAvatarUrl("https://example.com/jane.jpg");
        entity.setParentId(null);
        entity.setLikeCount(3L);
        entity.setIpAddress("192.168.0.108");
        entity.setDeleted(false);

        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        entity.setCreateTime(currentTime);
        entity.setUpdateTime(currentTime);
        entity.setCreateBy("testuser");
        entity.setUpdateBy("testuser");

        Method convertToDTOMethod = CommentService.class.getDeclaredMethod("convertToDTO", CommentEntity.class);
        convertToDTOMethod.setAccessible(true);

        CommentDTO dto = (CommentDTO) convertToDTOMethod.invoke(commentService, entity);

        assertEquals(456L, dto.getId());
        assertEquals("article-789", dto.getContentId());
        assertEquals("ARTICLE", dto.getContentType());
        assertEquals("Test comment for convertToDTO", dto.getContent());
        assertEquals(123L, dto.getUserId());
        assertEquals("Jane Doe", dto.getUserName());
        assertEquals("https://example.com/jane.jpg", dto.getUserAvatarUrl());
        assertNull(dto.getParentId());
        assertEquals(3L, dto.getLikeCount());
        assertEquals(false, dto.getDeleted());
        assertNotNull(dto.getCreateTime(), "CreateTime should not be null");
        assertNotNull(dto.getUpdateTime(), "UpdateTime should not be null");
        assertEquals(currentTime, dto.getCreateTime(), "CreateTime should match");
        assertEquals(currentTime, dto.getUpdateTime(), "UpdateTime should match");
    }

    @Test
    @DisplayName("Comment Service: JSON serialization test")
    void testJSONSerialization() throws Exception {
        CommentDTO dto = new CommentDTO();
        dto.setId(123L);
        dto.setContentId("article-456");
        dto.setContentType("ARTICLE");
        dto.setContent("Test comment");
        dto.setUserId(789L);
        dto.setUserName("John Doe");
        dto.setCreateTime(new Timestamp(System.currentTimeMillis()));
        dto.setUpdateTime(new Timestamp(System.currentTimeMillis()));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        String json = objectMapper.writeValueAsString(dto);

        assertTrue(json.contains("T"), "JSON should contain ISO format timestamp");
        assertTrue(json.contains("Z"), "JSON should contain UTC timezone indicator");
    }
}
