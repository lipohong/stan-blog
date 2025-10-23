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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;

import com.stan.blog.ai.dto.QuotaResponse;

public class AiServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    private ChatClient.Builder chatClientBuilder;

    private static class StubChatModel implements ChatModel {
        @Override
        public ChatResponse call(Prompt prompt) {
            return new ChatResponse(java.util.List.of(new Generation(new AssistantMessage("\"Great Title\"\nMore"))));
        }
    }

    private AiService aiService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        chatClientBuilder = ChatClient.builder(new StubChatModel());
        aiService = new AiService(stringRedisTemplate, chatClientBuilder.build());
        // configuration fields
        ReflectionTestUtils.setField(aiService, "dailyLimit", 5);
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
    @DisplayName("generateTitle: returns sanitized content from AI")
    void generateTitle_successAndSanitize() {
        String result = aiService.generateTitle("some input content");
        assertEquals("\"Great Title\" More", result);
    }




}