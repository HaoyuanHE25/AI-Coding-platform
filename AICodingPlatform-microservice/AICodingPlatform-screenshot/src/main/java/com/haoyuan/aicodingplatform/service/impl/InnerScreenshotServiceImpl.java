package com.haoyuan.aicodingplatform.service.impl;

import com.haoyuan.aicodingplatform.innerservice.InnerScreenshotService;
import com.haoyuan.aicodingplatform.service.ScreenshotService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

/**
 * Dubbo 服务提供者：对外暴露 InnerScreenshotService 契约。
 * 条件直接检查 COS 配置属性而非 @ConditionalOnBean：
 * COSClient 是 @Bean 方法产物，bean 定义在 REGISTER_BEAN 阶段才注册，
 * 而 Dubbo 扫描 @DubboService 的条件评估在 PARSE 阶段，@ConditionalOnBean 必然不匹配。
 * 因此统一用与 CosClientConfig 相同的 @ConditionalOnExpression 判断真实 COS 凭据。
 */
@DubboService
@ConditionalOnExpression(
        "'${cos.client.host:}' != '' and '${cos.client.secretId:}' != '' " +
        "and '${cos.client.secretKey:}' != '' and '${cos.client.region:}' != '' " +
        "and '${cos.client.bucket:}' != '' " +
        "and !'${cos.client.host:}'.startsWith('your-')"
)
public class InnerScreenshotServiceImpl implements InnerScreenshotService {

    @Resource
    private ScreenshotService screenshotService;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        return screenshotService.generateAndUploadScreenshot(webUrl);
    }
}
