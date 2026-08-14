package com.tachibana.langchain4jdemo.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 上下文记忆配置
 * <p>
 * 为每个会话 ID 创建一个滑动窗口记忆（保留最近 N 条消息），
 * 同一会话内的对话共享上下文，不同会话之间相互隔离。
 */
@Configuration
@Slf4j
public class ChatMemoryConfig {

    /** 每个会话保留的最大消息条数（超出后自动淘汰最旧消息） */
    private static final int MAX_MESSAGES = 20;

    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> {
            log.debug("创建会话记忆: {}", memoryId);
            return MessageWindowChatMemory.builder()
                    .id(memoryId)
                    .maxMessages(MAX_MESSAGES)
                    .build();
        };
    }
}
