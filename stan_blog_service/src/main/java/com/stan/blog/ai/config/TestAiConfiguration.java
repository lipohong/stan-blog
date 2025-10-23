package com.stan.blog.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;

import com.stan.blog.ai.service.AiService;

@Configuration
@Profile("test")
public class TestAiConfiguration {

    // Provide a lightweight RedisConnectionFactory for tests
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    // Provide StringRedisTemplate for AiService in tests
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

    @Bean
    public ChatClient chatClient() {
        return ChatClient.builder(new StubChatModel()).build();
    }

    @Bean
    public AiService aiService(StringRedisTemplate stringRedisTemplate, ChatClient chatClient) {
        return new AiService(stringRedisTemplate, chatClient);
    }

    static class StubChatModel implements ChatModel {
        @Override
        public ChatResponse call(Prompt prompt) {
            return new ChatResponse(java.util.List.of(new Generation(new AssistantMessage("Test Title"))));
        }
    }
}