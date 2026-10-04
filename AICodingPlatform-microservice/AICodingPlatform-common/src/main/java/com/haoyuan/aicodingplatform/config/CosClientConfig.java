package com.haoyuan.aicodingplatform.config;

import cn.hutool.core.util.StrUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯云COS配置类
 *
 * <p>条件说明：使用 @ConditionalOnExpression 而非 @ConditionalOnProperty，
 * 因为后者只要属性"存在"（哪怕是空字符串）就会匹配。这里用 SpEL 显式检查
 * 所有 5 个 cos.client.* 属性都是非空且非占位符值，确保只有真实的 COS 凭据
 * 才会激活 Configuration 和 cosClient Bean 的创建。
 *
 * <p>条件链：CosClientConfig / CosManager / ScreenshotServiceImpl / InnerScreenshotServiceImpl
 * 统一使用同一段 @ConditionalOnExpression（依赖 COS 配置属性非空，而非 bean 注册顺序），
 * 保证真实 COS 凭据存在时整条链同时激活。
 */
@Configuration
@ConfigurationProperties(prefix = "cos.client")
@ConditionalOnExpression(
        "'${cos.client.host:}' != '' and '${cos.client.secretId:}' != '' " +
        "and '${cos.client.secretKey:}' != '' and '${cos.client.region:}' != '' " +
        "and '${cos.client.bucket:}' != '' " +
        "and !'${cos.client.host:}'.startsWith('your-')"
)
@Data
@Slf4j
public class CosClientConfig {

    /**
     * 域名
     */
    private String host;

    /**
     * secretId
     */
    private String secretId;

    /**
     * 密钥（注意不要泄露）
     */
    private String secretKey;

    /**
     * 区域
     */
    private String region;

    /**
     * 桶名
     */
    private String bucket;

    @Bean
    public COSClient cosClient() {
        // 条件注解已保证所有字段非空，这里直接创建
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        log.info("COSClient 初始化成功，bucket={}, region={}", bucket, region);
        return new COSClient(cred, clientConfig);
    }
}
