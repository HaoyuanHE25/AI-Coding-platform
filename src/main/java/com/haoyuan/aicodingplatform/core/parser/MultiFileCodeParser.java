package com.haoyuan.aicodingplatform.core.parser;

import com.haoyuan.aicodingplatform.ai.model.MultiFileCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 多文件代码解析器（HTML + CSS + JS）
 *
 * 解析策略：
 *   1) 优先按 Markdown 围栏代码块（```html / ```css / ```js）提取
 *   2) 若围栏缺失，退化到结构启发式：
 *      - HTML：匹配 <!DOCTYPE html> ... </html> 片段
 *      - CSS：先从 <style>...</style> 标签提取，再在 HTML 上下文外按 CSS 语法行密度 ≥ 0.4 选最优段
 *      - JS：先从 <script>（无 src）...</script> 标签提取，再在 HTML 上下文外按 JS 语法行密度 ≥ 0.35 选最优段
 *
 * @author yupi
 */
public class MultiFileCodeParser implements CodeParser<MultiFileCodeResult> {

    // ---- 主路径：Markdown 围栏代码块匹配（宽容 \r\n / \n）----
    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\R([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern CSS_CODE_PATTERN  = Pattern.compile("```css\\s*\\R([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern JS_CODE_PATTERN   = Pattern.compile("```(?:js|javascript)\\s*\\R([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    // ---- 兜底启发式 ----
    private static final Pattern HTML_DOCTYPE_PATTERN  = Pattern.compile("(?i)(<!DOCTYPE\\s+html[\\s\\S]*?</html>)", Pattern.CASE_INSENSITIVE);
    private static final Pattern HTML_TAG_PATTERN      = Pattern.compile("(?is)<html[^>]*>([\\s\\S]*?)</html>");
    private static final Pattern STYLE_TAG_PATTERN     = Pattern.compile("(?is)<style[^>]*>([\\s\\S]*?)</style>");
    private static final Pattern SCRIPT_TAG_PATTERN    = Pattern.compile("(?is)<script(?![^>]*src=)[^>]*>([\\s\\S]*?)</script>");
    private static final Pattern CSS_LIKE_LINE         = Pattern.compile("(?m)^\\s*(?:[.#]?[\\w-]+\\s*\\{|@import\\b|@media\\b|@keyframes\\b|[:\\w-]+\\s*:\\s*[^;]+;|\\}\\s*$)");
    private static final Pattern JS_LIKE_LINE          = Pattern.compile("(?m)^\\s*(?:const\\b|let\\b|var\\b|function\\b|class\\b|import\\b|export\\b|return\\b|if\\s*\\(|for\\s*\\(|while\\s*\\(|switch\\s*\\(|try\\s*\\{|catch\\s*\\(|else\\b|=>\\s*\\{?|document\\.|window\\.|console\\.|\\w+\\s*=\\s*.+[({;]|\\w+\\(.*\\)\\s*;?)");

    @Override
    public MultiFileCodeResult parseCode(String codeContent) {
        MultiFileCodeResult result = new MultiFileCodeResult();

        // 1) 围栏提取
        String htmlCode = extractByPattern(codeContent, HTML_CODE_PATTERN);
        String cssCode  = extractByPattern(codeContent, CSS_CODE_PATTERN);
        String jsCode   = extractByPattern(codeContent, JS_CODE_PATTERN);

        // 2) 兜底启发式
        if (htmlCode == null || htmlCode.trim().isEmpty()) {
            htmlCode = extractHtmlByHeuristic(codeContent);
        }
        if (cssCode == null || cssCode.trim().isEmpty()) {
            cssCode = extractCssByHeuristic(codeContent, htmlCode);
        }
        if (jsCode == null || jsCode.trim().isEmpty()) {
            jsCode = extractJsByHeuristic(codeContent, htmlCode);
        }

        // 3) 设置结果
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        }
        if (cssCode != null && !cssCode.trim().isEmpty()) {
            result.setCssCode(cssCode.trim());
        }
        if (jsCode != null && !jsCode.trim().isEmpty()) {
            result.setJsCode(jsCode.trim());
        }
        return result;
    }

    // ==================== 围栏提取 ====================

    private String extractByPattern(String content, Pattern pattern) {
        Matcher m = pattern.matcher(content);
        return m.find() ? m.group(1) : null;
    }

    // ==================== 兜底：HTML ====================

    private String extractHtmlByHeuristic(String content) {
        Matcher doctype = HTML_DOCTYPE_PATTERN.matcher(content);
        if (doctype.find()) {
            return doctype.group(1);
        }
        Matcher htmlTag = HTML_TAG_PATTERN.matcher(content);
        if (htmlTag.find()) {
            return htmlTag.group(0);
        }
        return null;
    }

    // ==================== 兜底：CSS ====================

    private String extractCssByHeuristic(String content, String htmlCode) {
        // a) 从 <style> 标签中提取
        StringBuilder sb = new StringBuilder();
        Matcher style = STYLE_TAG_PATTERN.matcher(content);
        while (style.find()) {
            String s = style.group(1);
            if (s != null && !s.trim().isEmpty()) {
                if (sb.length() > 0) sb.append('\n');
                sb.append(s.trim());
            }
        }
        if (sb.length() > 0) return sb.toString();

        // b) HTML 上下文外，按 CSS 行密度挑选
        String outside = stripHtmlContext(content, htmlCode);
        return pickBestBlockByDensity(outside, CSS_LIKE_LINE, 0.4);
    }

    // ==================== 兜底：JS ====================

    private String extractJsByHeuristic(String content, String htmlCode) {
        // a) 从 <script>（无 src）标签中提取
        StringBuilder sb = new StringBuilder();
        Matcher script = SCRIPT_TAG_PATTERN.matcher(content);
        while (script.find()) {
            String s = script.group(1);
            if (s != null && !s.trim().isEmpty()) {
                if (sb.length() > 0) sb.append('\n');
                sb.append(s.trim());
            }
        }
        if (sb.length() > 0) return sb.toString();

        // b) HTML 上下文外，按 JS 行密度挑选
        String outside = stripHtmlContext(content, htmlCode);
        return pickBestBlockByDensity(outside, JS_LIKE_LINE, 0.35);
    }

    // ==================== 辅助方法 ====================

    private String stripHtmlContext(String content, String htmlCode) {
        String s = content;
        if (htmlCode != null && !htmlCode.isEmpty()) {
            int idx = content.indexOf(htmlCode);
            if (idx >= 0) {
                s = content.substring(0, idx) + content.substring(idx + htmlCode.length());
            }
        }
        // 清理残留的 Markdown 围栏行和 HTML 注释占位行
        s = s.replaceAll("(?m)^\\s*```(?:[A-Za-z0-9_-]+)?\\s*$", "");
        s = s.replaceAll("(?m)^\\s*<!--.*?-->\\s*$", "");
        return s;
    }

    /**
     * 在文本中按"语法行密度"选取最佳代码段。
     * 从每个起点 i 开始，尝试拼接 i..j 的连续段，密度达标则记录最优。
     */
    private String pickBestBlockByDensity(String content, Pattern linePattern, double minDensity) {
        if (content == null || content.isEmpty()) return null;

        String[] paragraphs = content.split("(?m)\\n\\s*\\n");
        String best = null;
        double bestScore = -1.0;

        for (int i = 0; i < paragraphs.length; i++) {
            StringBuilder merged = new StringBuilder();
            for (int j = i; j < paragraphs.length; j++) {
                if (merged.length() > 0) merged.append("\n\n");
                merged.append(paragraphs[j]);
                DensityResult r = computeDensity(merged.toString(), linePattern);
                if (r == null) continue;
                double score = r.density * 1000.0 + Math.min(r.nonEmpty, 400);
                if (r.density >= minDensity && score > bestScore) {
                    bestScore = score;
                    best = merged.toString();
                }
            }
        }
        return best;
    }

    private DensityResult computeDensity(String text, Pattern linePattern) {
        String[] lines = text.split("\\n");
        int nonEmpty = 0;
        int hits = 0;
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            nonEmpty++;
            if (linePattern.matcher(line).find()) hits++;
        }
        if (nonEmpty == 0) return null;
        return new DensityResult(nonEmpty, (double) hits / nonEmpty);
    }

    private record DensityResult(int nonEmpty, double density) {}
}
