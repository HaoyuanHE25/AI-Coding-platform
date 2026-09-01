package com.haoyuan.aicodingplatform.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * RedisChatMemoryStore 配置类
 */
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedisChatMemoryStoreConfig {

    private String host;

    private int port;

    private String password;

    private long ttl;

    @Bean
    public RedisChatMemoryStore redisChatMemoryStore() {
        // 注意：langchain4j-community-redis 1.1.0-beta7 的 RedisChatMemoryStore
        // 仅当 user 非空时才会使用 password（否则 Jedis 不带认证连接）。
        // 因此有密码时需指定 Redis 默认用户 "default"；本地无密码则不设 user。
        boolean hasPassword = password != null && !password.isEmpty();
        return RedisChatMemoryStore.builder()
                .host(host)
                .port(port)
                .user(hasPassword ? "default" : null)
                .password(password)
                .ttl(ttl)
                .build();
    }
}
