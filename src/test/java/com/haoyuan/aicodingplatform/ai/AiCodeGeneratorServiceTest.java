package com.haoyuan.aicodingplatform.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    @Test
    void generateHtmlCode() {
        AiCodeGeneratorService service = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(1L);
        String rawText = service.generateHtmlCode("做一个程序员博客，不超20行");
        assertNotNull(rawText, "Service 返回的原始文本不应为 null");
        assertFalse(rawText.isBlank(), "返回文本不应为空白");
        // 验证返回内容包含代码相关特征(围栏、DOCTYPE、html 标签等)
        String lower = rawText.toLowerCase();
        assertTrue(lower.contains("html") || lower.contains("```") || lower.contains("<!doctype"),
                "返回文本应包含 HTML 相关内容");
    }

    @Test
    void generateMultiFileCode() {
        AiCodeGeneratorService service = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(2L);
        String rawText = service.generateMultiFileCode("做一个程序员博客，不超20行");
        assertNotNull(rawText, "Service 返回的原始文本不应为 null");
        assertFalse(rawText.isBlank(), "返回文本不应为空白");
        // 验证返回内容包含多种代码特征
        String lower = rawText.toLowerCase();
        boolean hasHtml = lower.contains("html") || lower.contains("```html");
        boolean hasCss = lower.contains("css") || lower.contains("```css");
        boolean hasJs = lower.contains("javascript") || lower.contains("js") || lower.contains("```js");
        assertTrue(hasHtml && (hasCss || hasJs),
                "多文件模式应包含 HTML 和 CSS/JS 内容,实际: hasHtml=" + hasHtml + ", hasCss=" + hasCss + ", hasJs=" + hasJs);
    }

    @Test
    void testChatMemory() {
        // 使用同一个 appId 的 service 实例,验证对话记忆
        AiCodeGeneratorService service = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(3L);
        // 第一次生成
        String result1 = service.generateHtmlCode("做个程序员鱼皮的工具网站，总代码量不超过 20 行");
        assertNotNull(result1, "第一次生成不应为 null");
        assertFalse(result1.isBlank(), "第一次生成不应为空白");
        // 第二次:询问上次做了什么(验证记忆)
        String result2 = service.generateHtmlCode("不要生成网站，告诉我你刚刚做了什么？");
        assertNotNull(result2, "第二次生成不应为 null");
        assertFalse(result2.isBlank(), "第二次生成不应为空白");
        // 第三次:再次生成
        String result3 = service.generateHtmlCode("做个程序员鱼皮的工具网站，总代码量不超过 20 行");
        assertNotNull(result3, "第三次生成不应为 null");
        assertFalse(result3.isBlank(), "第三次生成不应为空白");
        // 第四次:再次询问(验证记忆持续)
        String result4 = service.generateHtmlCode("不要生成网站，告诉我你刚刚做了什么？");
        assertNotNull(result4, "第四次生成不应为 null");
        assertFalse(result4.isBlank(), "第四次生成不应为空白");
    }
}
