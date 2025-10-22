package com.stan.blog.ai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.stan.blog.ai.dto.QuotaResponse;

public class AiServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @Mock
    private RestTemplate restTemplate;

    private AiService aiService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        aiService = new AiService(stringRedisTemplate);
        // inject mock RestTemplate and configuration fields
        ReflectionTestUtils.setField(aiService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(aiService, "apiKey", "test-key");
        ReflectionTestUtils.setField(aiService, "baseUrl", "http://localhost");
        ReflectionTestUtils.setField(aiService, "model", "deepseek-chat");
        ReflectionTestUtils.setField(aiService, "dailyLimit", 5);
        ReflectionTestUtils.setField(aiService, "temperature", 0.2d);
        ReflectionTestUtils.setField(aiService, "maxTokens", 128);
        ReflectionTestUtils.setField(aiService, "systemPrompt", "You are a helpful assistant that writes concise, attractive blog titles.");
    }

    @Test
    @DisplayName("Admin quota: allowed=true, remaining=MAX")
    void checkQuota_adminUnlimited() {
        QuotaResponse resp = aiService.checkQuota(123L, true);
        assertTrue(resp.isAllowed());
        assertEquals(Integer.MAX_VALUE, resp.getRemaining());
    }

    @Test
    @DisplayName("Non-admin quota: remaining computed from Redis usage")
    void checkQuota_nonAdminRemaining() {
        when(valueOps.get(anyString())).thenReturn("2");
        QuotaResponse resp = aiService.checkQuota(456L, false);
        assertTrue(resp.isAllowed());
        assertEquals(3, resp.getRemaining());
    }

    @Test
    @DisplayName("increaseUsage: set 24h TTL only when first increment")
    void increaseUsage_setsExpiryOnFirstIncrement() {
        when(valueOps.increment(anyString())).thenReturn(1L);
        aiService.increaseUsage(789L);
        verify(stringRedisTemplate, times(1)).expire(anyString(), eq(Duration.ofHours(24)));
    }

    @Test
    @DisplayName("increaseUsage: no TTL set when count > 1")
    void increaseUsage_noExpiryWhenNotFirst() {
        when(valueOps.increment(anyString())).thenReturn(2L);
        aiService.increaseUsage(789L);
        verify(stringRedisTemplate, never()).expire(anyString(), any());
    }

    @Test
    @DisplayName("generateTitle: parses choices/message/content and sanitizes output")
    void generateTitle_successAndSanitize() {
        Map<String, Object> body = new HashMap<>();
        body.put("choices", List.of(Map.of("message", Map.of("content", "\"Great Title\"\nMore"))));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(body));

        String result = aiService.generateTitle("some input content");
        assertEquals("\"Great Title\" More", result);
    }

    @Test
    @DisplayName("generateTitle: empty body throws")
    void generateTitle_emptyBodyThrows() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(null));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> aiService.generateTitle("x"));
        assertTrue(ex.getMessage().contains("Empty response"));
    }

    @Test
    @DisplayName("generateTitle: missing choices throws")
    void generateTitle_missingChoicesThrows() {
        Map<String, Object> body = new HashMap<>();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(body));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> aiService.generateTitle("x"));
        assertTrue(ex.getMessage().contains("choices"));
    }

    @Test
    @DisplayName("generateTitle: missing message throws")
    void generateTitle_missingMessageThrows() {
        Map<String, Object> body = new HashMap<>();
        body.put("choices", List.of(Map.of()));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(body));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> aiService.generateTitle("x"));
        assertTrue(ex.getMessage().contains("message"));
    }

    @Test
    @DisplayName("generateTitle: missing content throws")
    void generateTitle_missingContentThrows() {
        Map<String, Object> body = new HashMap<>();
        body.put("choices", List.of(Map.of("message", Map.of())));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(body));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> aiService.generateTitle("x"));
        assertTrue(ex.getMessage().contains("content"));
    }
}