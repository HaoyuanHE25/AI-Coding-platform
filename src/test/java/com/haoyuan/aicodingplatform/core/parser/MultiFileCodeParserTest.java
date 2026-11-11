package com.haoyuan.aicodingplatform.core.parser;

import com.haoyuan.aicodingplatform.ai.model.MultiFileCodeResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MultiFileCodeParser 针对性测试：验证围栏正则匹配及兜底启发式
 */
class MultiFileCodeParserTest {

    private final MultiFileCodeParser parser = new MultiFileCodeParser();

    @Test
    void parseFromFencedBlocks() {
        // 标准三围栏：html / css / js，各自独立闭合
        String input = """
                示例说明文字

                <!-- index.html -->
                ```html
                <!DOCTYPE html>
                <html>
                <head>
                    <title>测试</title>
                    <link rel="stylesheet" href="style.css">
                </head>
                <body>
                    <h1>Hello</h1>
                    <script src="script.js"></script>
                </body>
                </html>
                ```

                <!-- style.css -->
                ```css
                h1 { color: blue; text-align: center; }
                .card { padding: 16px; }
                ```

                <!-- script.js -->
                ```js
                console.log('load');
                document.addEventListener('DOMContentLoaded', () => {
                    console.log('ready');
                });
                ```

                生成完成！
                """;

        MultiFileCodeResult result = parser.parseCode(input);
        assertNotNull(result);
        assertNotNull(result.getHtmlCode(), "HTML 围栏应被正确解析");
        assertNotNull(result.getCssCode(), "CSS 围栏应被正确解析");
        assertNotNull(result.getJsCode(), "JS 围栏应被正确解析");

        System.out.println("=== HTML ===");
        System.out.println(result.getHtmlCode());
        System.out.println("=== CSS ===");
        System.out.println(result.getCssCode());
        System.out.println("=== JS ===");
        System.out.println(result.getJsCode());
    }

    @Test
    void parseWithMissingFencesFallback() {
        // 模拟模型偶尔省略围栏、或围栏不完整的输出
        String input = """
                创建一个任务管理网站

                html 格式
                <!DOCTYPE html>
                <html>
                <head>
                    <title>任务管理</title>
                    <link rel="stylesheet" href="style.css">
                </head>
                <body>
                    <h1>任务列表</h1>
                    <script src="script.js"></script>
                </body>
                </html>

                css 格式
                h1 { color: #333; font-size: 24px; }
                .task { padding: 12px; border-bottom: 1px solid #eee; }
                .task:hover { background: #f5f5f5; }
                ```
                ```js
                const tasks = [];
                function addTask(title) {
                    tasks.push({ title, done: false });
                    render();
                }
                function render() {
                    console.log('rendering ' + tasks.length + ' tasks');
                }
                document.getElementById('addBtn').addEventListener('click', () => {
                    addTask('New task');
                });

                完成
                """;

        MultiFileCodeResult result = parser.parseCode(input);
        assertNotNull(result);
        assertNotNull(result.getHtmlCode(), "HTML 兜底：应通过 DOCTYPE 识别");
        assertNotNull(result.getCssCode(), "CSS 兜底：应通过 CSS 语法行密度识别");
        assertNotNull(result.getJsCode(), "JS 兜底：应通过 JS 语法行密度识别");

        System.out.println("=== Fallback HTML ===");
        System.out.println(result.getHtmlCode());
        System.out.println("=== Fallback CSS ===");
        System.out.println(result.getCssCode());
        System.out.println("=== Fallback JS ===");
        System.out.println(result.getJsCode());
    }

    @Test
    void parseConversationTextShouldReturnEmpty() {
        // 模拟普通对话文本(非代码请求)
        String input = """
                你好！我是 AI 助手，很高兴为你服务。
                我刚刚为你生成了一个任务管理网站，它支持添加、删除和标记完成任务。
                如果你有任何问题或需要修改，请随时告诉我！
                """;

        MultiFileCodeResult result = parser.parseCode(input);
        assertNotNull(result);
        assertNull(result.getHtmlCode(), "普通对话文本不应提取出 HTML 代码");
        assertNull(result.getCssCode(), "普通对话文本不应提取出 CSS 代码");
        assertNull(result.getJsCode(), "普通对话文本不应提取出 JS 代码");

        System.out.println("=== 对话文本解析结果 ===");
        System.out.println("htmlCode: " + result.getHtmlCode());
        System.out.println("cssCode: " + result.getCssCode());
        System.out.println("jsCode: " + result.getJsCode());
    }
}
