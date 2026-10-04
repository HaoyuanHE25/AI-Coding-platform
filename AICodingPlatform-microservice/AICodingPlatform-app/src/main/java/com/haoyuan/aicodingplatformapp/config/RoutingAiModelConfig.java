package com.haoyuan.aicodingplatformapp.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.routing-chat-model")
@Data
public class RoutingAiModelConfig {

    private String baseUrl;

    private String apiKey;

    private String modelName;

    private Integer maxTokens;

    /**
     * 路由是分类任务，默认温度 0 保证同一提示词的路由结果稳定可复现
     */
    private Double temperature = 0.0;

    private Boolean logRequests = false;

    private Boolean logResponses = false;

    /**
     * 创建用于路由判断的ChatModel
     * <p>
     * bean 名称 routingChatModelPrototype 被 AiCodeGenTypeRoutingServiceFactory 按名引用，
     * 重命名时两处需同步修改；prototype 作用域使每次路由调用都拿到独立实例。
     */
    @Bean
    @Scope("prototype")
    public ChatModel routingChatModelPrototype() {
        if (apiKey == null || apiKey.isBlank() || baseUrl == null || baseUrl.isBlank()) {
            log.warn("langchain4j.open-ai.routing-chat-model 的 api-key / base-url 未配置，"
                    + "智能路由调用将失败并回退到默认代码生成类型");
        }
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .maxTokens(maxTokens)
                .temperature(temperature)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .build();
    }
}
