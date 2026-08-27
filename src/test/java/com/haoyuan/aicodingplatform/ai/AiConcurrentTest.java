package com.haoyuan.aicodingplatform.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class AiConcurrentTest {

    // 直接注入自动装配的 openAiChatModel（具备有效的 langchain4j.open-ai.chat-model 配置）。
    // 不再依赖 AiCodeGenTypeRoutingServiceFactory：其内部通过 SpringContextUtil 获取的
    // routingChatModelPrototype 缺少 routing-chat-model 配置（apiKey/baseUrl 为空）会导致调用失败。
    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    // 旧实现：依赖 AiCodeGenTypeRoutingServiceFactory，已注释。
    // @Resource
    // @Qualifier("aiCodeGenTypeRoutingServiceFactory")
    // private AiCodeGenTypeRoutingServiceFactory routingServiceFactory;

    @Test
    public void testConcurrentRoutingCalls() throws InterruptedException {
        String[] prompts = {
                "做一个简单的HTML页面",
                "做一个多页面网站项目",
                "做一个Vue管理系统"
        };
        // 使用虚拟线程并发执行
        Thread[] threads = new Thread[prompts.length];
        for (int i = 0; i < prompts.length; i++) {
            final String prompt = prompts[i];
            final int index = i + 1;
            threads[i] = Thread.ofVirtual().start(() -> {
                AiCodeGenTypeRoutingService service = AiServices.builder(AiCodeGenTypeRoutingService.class)
                        .chatModel(chatModel)
                        .build();
                var result = service.routeCodeGenType(prompt);
                log.info("线程 {}: {} -> {}", index, prompt, result.getValue());
            });
        }
        // 等待所有任务完成
        for (Thread thread : threads) {
            thread.join();
        }
    }
}
