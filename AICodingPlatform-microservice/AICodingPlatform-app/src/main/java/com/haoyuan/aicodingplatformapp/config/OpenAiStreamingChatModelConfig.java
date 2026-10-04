package com.haoyuan.aicodingplatformapp.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * 流式模型配置（用于 HTML/多文件代码生成）。
 * <p>
 * 原先 openAiStreamingChatModel 由 langchain4j-open-ai-spring-boot4-starter 自动装配,
 * 但 starter 的自动装配类位于依赖 jar,由父类加载器加载,而本项目内对 langchain4j
 * 内部类的本地覆盖(dev.langchain4j.*)由 devtools 重启类加载器加载,导致自动装配创建的
 * bean 与注入点声明的 StreamingChatModel 类型分属两个类加载器,启动时报
 * BeanNotOfRequiredTypeException。
 * <p>
 * 因此这里在项目代码内手动创建该 bean(与 reasoningStreamingChatModel 相同的做法),
 * 读取 langchain4j.open-ai.chat-model 下的公共连接配置,并移除
 * application-local.yml 中的 streaming-chat-model 配置块以停用自动装配。
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
@Data
public class OpenAiStreamingChatModelConfig {



    private String baseUrl;

    private String apiKey;

    private String modelName;

    private Integer maxTokens;

    private Boolean logRequests;

    private Boolean logResponses;

    private Duration timeout;

    @Bean
    public StreamingChatModel openAiStreamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .timeout(timeout)
                .build();
    }
}
