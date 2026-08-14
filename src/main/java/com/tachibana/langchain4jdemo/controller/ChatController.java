package com.tachibana.langchain4jdemo.controller;

import com.tachibana.langchain4jdemo.common.Result;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 对话接口
 */
@RestController
@Slf4j
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final OpenAiChatModel model;

    /**
     * 同步对话接口（GET），适合浏览器地址栏快速测试
     *
     * @param message 用户消息
     */
    @GetMapping("/send")
    public Result<String> sendGet(@RequestParam String message) {
        return doChat(message);
    }

    /**
     * 同步对话接口（POST），前端页面调用
     * <p>
     * 请求体: {"message": "你好"}
     */
    @PostMapping("/send")
    public Result<String> sendPost(@RequestBody Map<String, String> body) {
        String message = body == null ? null : body.get("message");
        return doChat(message);
    }

    private Result<String> doChat(String message) {
        if (message == null || message.isBlank()) {
            return Result.error(400, "message 不能为空");
        }
        log.info("收到用户消息: {}", message);
        long start = System.currentTimeMillis();
        try {
            String reply = model.chat(message.trim());
            log.info("AI 回复耗时: {} ms", System.currentTimeMillis() - start);
            return Result.success(reply);
        } catch (Exception e) {
            log.error("调用大模型失败", e);
            return Result.error(500, "调用大模型失败: " + e.getMessage());
        }
    }
}
