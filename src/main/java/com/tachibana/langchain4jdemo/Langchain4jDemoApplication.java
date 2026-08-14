package com.tachibana.langchain4jdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Web 应用主类
 * <p>
 * 排除 {@code cli} 包：{@code ChatCli} 是独立的命令行程序，
 * 不应在 Web 应用启动时被扫描并执行其 CommandLineRunner。
 */
@SpringBootApplication
@ComponentScan(
        basePackages = "com.tachibana.langchain4jdemo",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.tachibana\\.langchain4jdemo\\.cli\\..*"
        )
)
@Slf4j
public class Langchain4jDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(Langchain4jDemoApplication.class, args);
        log.info("Langchain4jDemo 启动成功，访问 http://localhost:8080 打开聊天页面");
    }

}
