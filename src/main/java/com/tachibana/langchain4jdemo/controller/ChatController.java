package com.tachibana.langchain4jdemo.controller;

import com.tachibana.langchain4jdemo.agent.Assistant;
import com.tachibana.langchain4jdemo.common.Result;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * AI 对话接口
 */
@RestController
@Slf4j
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final OpenAiChatModel model;
    private final Assistant assistant;

    /** 未传 sessionId 时使用的默认会话 */
    private static final String DEFAULT_SESSION = "default";

    /**
     * 同步对话接口（GET），适合浏览器地址栏快速测试，无记忆
     *
     * @param message 用户消息
     */
    @GetMapping("/send")
    public Result<String> sendGet(@RequestParam String message) {
        return doChat(message, UUID.randomUUID().toString(), false);
    }

    /**
     * 带上下文记忆的对话接口（POST），前端页面调用
     * <p>
     * 请求体: {"message": "你好", "sessionId": "xxx"}
     * <ul>
     *   <li>同一 sessionId 的对话共享上下文记忆</li>
     *   <li>sessionId 缺省时使用默认会话</li>
     * </ul>
     */
    @PostMapping("/send")
    public Result<String> sendPost(@RequestBody Map<String, String> body) {
        String message = body == null ? null : body.get("message");
        String sessionId = body == null ? null : body.get("sessionId");
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = DEFAULT_SESSION;
        }
        return doChat(message, sessionId, true);
    }

    /**
     * 带记忆的流式对话接口（SSE），供前端打字机效果使用
     * <p>
     * 返回 <code>text/event-stream</code>，数据流为：
     * <ul>
     *   <li>正常回复：逐字推送 token 文本</li>
     *   <li>完成：推送 <code>[DONE]</code></li>
     *   <li>出错：推送 JSON <code>{"error":"..."}</code></li>
     * </ul>
     *
     * @param message   用户消息
     * @param sessionId 会话 ID（可选，缺省使用默认会话）
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String message,
                             @RequestParam(required = false) String sessionId) {
        // 解析后的会话 ID（final，供 lambda 使用）
        final String resolvedSessionId =
                (sessionId == null || sessionId.isBlank()) ? DEFAULT_SESSION : sessionId;

        // 2 分钟超时（DeepSeek 长回答可能较慢）
        SseEmitter emitter = new SseEmitter(120_000L);

        if (message == null || message.isBlank()) {
            sendEvent(emitter, "{\"error\":\"message 不能为空\"}");
            emitter.complete();
            return emitter;
        }

        log.info("[会话:{}] 流式收到用户消息: {}", resolvedSessionId, message);
        long start = System.currentTimeMillis();

        assistant.chatStream(resolvedSessionId, message.trim())
                .onPartialResponse(token -> sendEvent(emitter, token))
                .onCompleteResponse(response -> {
                    sendEvent(emitter, "[DONE]");
                    emitter.complete();
                    log.info("[会话:{}] 流式回复耗时: {} ms", resolvedSessionId, System.currentTimeMillis() - start);
                })
                .onError(e -> {
                    log.error("[会话:{}] 流式调用大模型失败", resolvedSessionId, e);
                    sendEvent(emitter, "{\"error\":\"调用大模型失败: " + e.getMessage() + "\"}");
                    emitter.complete();
                })
                .start();

        return emitter;
    }

    /** 向 SSE 客户端推送事件，客户端断开时静默忽略 */
    private void sendEvent(SseEmitter emitter, String data) {
        try {
            emitter.send(SseEmitter.event().data(data));
        } catch (IOException e) {
            log.warn("SSE 客户端断开: {}", e.getMessage());
        }
    }

    private Result<String> doChat(String message, String sessionId, boolean withMemory) {
        if (message == null || message.isBlank()) {
            return Result.error(400, "message 不能为空");
        }
        log.info("[会话:{}] 收到用户消息: {}", sessionId, message);
        long start = System.currentTimeMillis();
        try {
            String reply = withMemory
                    ? assistant.chat(sessionId, message.trim())
                    : model.chat(message.trim());
            log.info("[会话:{}] AI 回复耗时: {} ms", sessionId, System.currentTimeMillis() - start);
            return Result.success(reply);
        } catch (Exception e) {
            log.error("[会话:{}] 调用大模型失败", sessionId, e);
            return Result.error(500, "调用大模型失败: " + e.getMessage());
        }
    }
}

