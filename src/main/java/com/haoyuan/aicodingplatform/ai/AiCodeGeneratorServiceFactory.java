package com.haoyuan.aicodingplatform.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 代码生成服务工厂。
 *
 * 通过编程式 AiServices.builder() 显式创建 AiCodeGeneratorService 代理 bean。
 * ChatModel 由 langchain4j-open-ai-spring-boot4-starter 自动装配,
 * AiCodeGeneratorService 接口方法上的 @SystemMessage(fromResource) 注解
 * 由 AiServices 内部反射解析,与 @AiService 注解无关。
 */
@Configuration
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .build();
    }
}
