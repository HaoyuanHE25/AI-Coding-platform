package com.haoyuan.aicodingplatformapp;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
// app 模块的 UserServiceImpl 直接复用了 user 模块的 UserMapper，需一并纳入扫描
@MapperScan({"com.haoyuan.aicodingplatformapp.mapper", "com.haoyuan.aicodingplatformuser.mapper"})
// 显式限定扫描范围：本服务自身包 + 公共模块中被依赖的组件包。
// 注意不能直接扫 com.haoyuan.aicodingplatform 全量：app 模块内已复制了 common/ai 的
// JsonConfig、CrossConfig、CosClientConfig、OpenAiStreamingChatModelConfig、
// ReasoningStreamingChatModelConfig、RoutingAiModelConfig 六个同名配置类，
// 全量扫描会触发 ConflictingBeanDefinitionException 与 @Bean 方法同名冲突。
@ComponentScan({
        "com.haoyuan.aicodingplatformapp",
        "com.haoyuan.aicodingplatform.ai.tools",
        "com.haoyuan.aicodingplatform.utils"
})

@EnableCaching
@EnableDubbo
public class AiCodingplatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiCodingplatformApplication.class, args);
    }
}
