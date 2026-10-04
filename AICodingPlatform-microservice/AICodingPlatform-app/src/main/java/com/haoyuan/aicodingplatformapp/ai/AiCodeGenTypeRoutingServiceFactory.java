package com.haoyuan.aicodingplatformapp.ai;

import com.haoyuan.aicodingplatform.ai.AiCodeGenTypeRoutingService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
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

    /**
     * 路由模型 bean 名称：由 RoutingAiModelConfig 读取
     * langchain4j.open-ai.routing-chat-model 配置创建的 prototype ChatModel
     */
    private static final String ROUTING_CHAT_MODEL_BEAN_NAME = "routingChatModelPrototype";

    /**
     * 构造器注入 ApplicationContext，不再使用 SpringContextUtil：
     * 其静态 applicationContext 要等自身初始化后才被赋值，而本类的 @Bean 方法在
     * 上下文刷新期间就会被调用，静态取用会抛 NPE。
     */
    private final ApplicationContext applicationContext;

    public AiCodeGenTypeRoutingServiceFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 创建AI代码生成类型路由服务实例
     */
    public AiCodeGenTypeRoutingService createAiCodeGenTypeRoutingService() {
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(routingChatModel())
                .build();
    }

    /**
     * 默认提供一个 Bean
     */
    @Bean
    public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService() {
        return createAiCodeGenTypeRoutingService();
    }

    /**
     * 按名称获取路由专用模型；prototype 作用域保证每次调用都是新实例，
     * 避免多个并发请求共用同一个 ChatModel。
     */
    private ChatModel routingChatModel() {
        return applicationContext.getBean(ROUTING_CHAT_MODEL_BEAN_NAME, ChatModel.class);
    }
}
