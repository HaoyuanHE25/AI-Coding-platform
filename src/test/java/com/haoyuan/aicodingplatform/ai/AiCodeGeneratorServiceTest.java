package com.haoyuan.aicodingplatform.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        String rawText = aiCodeGeneratorService.generateHtmlCode("做一个程序员博客，不超20行");
        assertNotNull(rawText, "Service 返回的原始文本不应为 null");
        assertFalse(rawText.isBlank(), "返回文本不应为空白");
        // 验证返回内容包含代码相关特征(围栏、DOCTYPE、html 标签等)
        String lower = rawText.toLowerCase();
        assertTrue(lower.contains("html") || lower.contains("```") || lower.contains("<!doctype"),
                "返回文本应包含 HTML 相关内容");
    }

    @Test
    void generateMultiFileCode() {
        String rawText = aiCodeGeneratorService.generateMultiFileCode("做一个程序员博客，不超20行");
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
}
