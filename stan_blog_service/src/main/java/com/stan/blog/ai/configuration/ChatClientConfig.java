package com.stan.blog.ai.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class ChatClientConfig {
  @Bean
  public ChatClient deepSeekChatClient(DeepSeekChatModel chatModel) {
      return ChatClient.create(chatModel);
  }
}
