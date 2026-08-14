package com.tachibana.langchain4jdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class Langchain4jDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(Langchain4jDemoApplication.class, args);
        log.info("Langchain4jDemo 启动成功，访问 http://localhost:8080 打开聊天页面");
    }

}
