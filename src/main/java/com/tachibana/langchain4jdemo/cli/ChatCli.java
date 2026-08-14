package com.tachibana.langchain4jdemo.cli;

import com.tachibana.langchain4jdemo.agent.Assistant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Scanner;
import java.util.UUID;

/**
 * 命令行聊天客户端（CLI）
 * <p>
 * 在终端中运行（无需启动 Web 服务，独立进程）：
 * <pre>
 *   .\mvnw.cmd spring-boot:run "-Dspring-boot.run.main-class=com.tachibana.langchain4jdemo.cli.ChatCli"
 * </pre>
 * <p>
 * 支持命令：
 * <ul>
 *   <li>/help  显示帮助</li>
 *   <li>/new   开启新会话（清空上下文记忆）</li>
 *   <li>/exit  退出程序</li>
 * </ul>
 */
@SpringBootApplication(scanBasePackages = "com.tachibana.langchain4jdemo")
@Slf4j
public class ChatCli implements CommandLineRunner {

    private final Assistant assistant;

    public ChatCli(Assistant assistant) {
        this.assistant = assistant;
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(ChatCli.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Override
    public void run(String... args) {
        // 每个 CLI 进程一个会话 ID，支持 /new 更换以清空记忆
        String sessionId = UUID.randomUUID().toString();
        Scanner scanner = new Scanner(System.in, "UTF-8");

        printBanner();

        while (true) {
            System.out.print("\n你 [" + shortId(sessionId) + "]> ");
            String line;
            try {
                line = scanner.nextLine();
            } catch (Exception e) {
                break; // Ctrl+C / EOF
            }
            if (line == null) {
                break;
            }
            String input = line.trim();
            if (input.isEmpty()) {
                continue;
            }

            // ---- 内置命令 ----
            if (input.equalsIgnoreCase("/exit") || input.equalsIgnoreCase("/quit")) {
                System.out.println("再见！👋");
                break;
            }
            if (input.equalsIgnoreCase("/help")) {
                printHelp();
                continue;
            }
            if (input.equalsIgnoreCase("/new")) {
                sessionId = UUID.randomUUID().toString();
                System.out.println("✅ 已开启新会话，上下文记忆已清空");
                continue;
            }

            // ---- 正常聊天（带记忆） ----
            try {
                String reply = assistant.chat(sessionId, input);
                System.out.println("AI> " + reply);
            } catch (Exception e) {
                System.err.println("❌ 调用大模型失败: " + e.getMessage());
            }
        }
    }

    private void printBanner() {
        System.out.println();
        System.out.println("==================================================");
        System.out.println("   🤖 LangChain4j AI 助手 (CLI)");
        System.out.println("   模型: DeepSeek · 支持多轮上下文记忆");
        System.out.println("   输入 /help 查看帮助，/exit 退出");
        System.out.println("==================================================");
    }

    private void printHelp() {
        System.out.println("""
                可用命令:
                  /help   显示帮助
                  /new    开启新会话（清空上下文记忆）
                  /exit   退出程序
                其他输入将作为消息发送给 AI。
                """);
    }

    private String shortId(String id) {
        return id.substring(0, Math.min(8, id.length()));
    }
}
