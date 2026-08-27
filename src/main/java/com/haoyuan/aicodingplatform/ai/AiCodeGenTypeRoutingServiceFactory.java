package com.haoyuan.aicodingplatform.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI代码生成类型路由服务工厂
 *
 * @author yupi
 */
@Slf4j
@Configuration
public class AiCodeGenTypeRoutingServiceFactory {

    // 直接注入自动装配的 openAiChatModel（具备有效的 langchain4j.open-ai.chat-model 配置）。
    // 不再通过 SpringContextUtil 获取 routingChatModelPrototype：
    // 1) @Bean 在上下文刷新期间被调用时，SpringContextUtil 的静态 applicationContext 尚未注入，会抛 NPE；
    // 2) routing-chat-model 配置缺失，routingChatModelPrototype 的 apiKey/baseUrl 为空无法调用。
    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    /**
     * 创建AI代码生成类型路由服务实例
     */
    public AiCodeGenTypeRoutingService createAiCodeGenTypeRoutingService() {
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(chatModel)
                .build();
    }

    /**
     * 默认提供一个 Bean
     */
    @Bean
    public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService() {
        return createAiCodeGenTypeRoutingService();
    }
}
