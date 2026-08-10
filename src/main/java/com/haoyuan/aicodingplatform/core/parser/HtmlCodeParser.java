package com.haoyuan.aicodingplatform.core.parser;

import com.haoyuan.aicodingplatform.ai.model.HtmlCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML 单文件代码解析器
 *
 * 解析策略：
 *   1) 优先按 Markdown 围栏 ```html ... ``` 提取
 *   2) 若围栏缺失，退化到结构启发式：
 *      - 匹配 <!DOCTYPE html> ... </html> 片段
 *      - 或 <html> ... </html> 片段
 *      - 兜底：将整个原文作为 HTML
 *
 * @author yupi
 */
public class HtmlCodeParser implements CodeParser<HtmlCodeResult> {

    // 主路径：Markdown 围栏（宽容 \r\n / \n）
    private static final Pattern HTML_FENCE_PATTERN =
            Pattern.compile("```html\\s*\\R([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    // 兜底
    private static final Pattern DOCTYPE_PATTERN =
            Pattern.compile("(?i)(<!DOCTYPE\\s+html[\\s\\S]*?</html>)", Pattern.CASE_INSENSITIVE);
    private static final Pattern HTML_TAG_PATTERN =
            Pattern.compile("(?is)<html[^>]*>([\\s\\S]*?)</html>");

    @Override
    public HtmlCodeResult parseCode(String codeContent) {
        HtmlCodeResult result = new HtmlCodeResult();

        String htmlCode = extractHtmlByFence(codeContent);

        if (htmlCode == null || htmlCode.trim().isEmpty()) {
            htmlCode = extractHtmlByHeuristic(codeContent);
        }

        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        } else {
            // 最终兜底：返回全部原文
            result.setHtmlCode(codeContent.trim());
        }
        return result;
    }

    private String extractHtmlByFence(String content) {
        Matcher m = HTML_FENCE_PATTERN.matcher(content);
        return m.find() ? m.group(1) : null;
    }

    private String extractHtmlByHeuristic(String content) {
        // DOCTYPE 优先
        Matcher doctype = DOCTYPE_PATTERN.matcher(content);
        if (doctype.find()) {
            return doctype.group(1);
        }
        // 无 DOCTYPE 的 <html> 标签
        Matcher htmlTag = HTML_TAG_PATTERN.matcher(content);
        if (htmlTag.find()) {
            return htmlTag.group(0);
        }
        return null;
    }
}
