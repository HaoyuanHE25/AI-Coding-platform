package com.haoyuan.aicodingplatform.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * AI 代码生成服务。
 *
 * 职责只有一个：调用 LLM 并返回**原始文本**。返回的文本中，HTML 模式包含一段
 * ` ```html ... ``` ` Markdown 围栏代码块；多文件模式包含三对独立闭合的
 * ` ```html / ```css / ```js ` 围栏代码块。
 *
 * 结构化解析（提取 html/css/js 代码到 HtmlCodeResult/MultiFileCodeResult）
 * 由 AiCodeGeneratorFacade 统一调用 CodeParserExecutor 完成；文件落盘由
 * CodeFileSaverExecutor 完成，避免与 LLM 的输出格式耦合。
 *
 * 实例化：AiCodeGeneratorServiceFactory 工厂类通过
 * AiServices.builder(AiCodeGeneratorService.class).chatModel(chatModel).build()
 * 显式创建为 Spring bean。
 */
public interface AiCodeGeneratorService {

    /**
     * 生成 HTML 单页代码
     *
     * @param userMessage 用户对网站的描述
     * @return 包含一个 ` ```html ` 围栏代码块的模型原始响应文本
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    String generateHtmlCode(@UserMessage String userMessage);

    /**
     * 生成多文件代码（HTML + CSS + JS）
     *
     * @param userMessage 用户对网站的描述
     * @return 包含三对独立闭合的 html/css/js 围栏代码块的模型原始响应文本
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    String generateMultiFileCode(@UserMessage String userMessage);

    /**
     * 流式生成 HTML 单页代码
     *
     * @param userMessage 用户对网站的描述
     * @return 模型按 SSE/分块推送的字符串序列
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(@UserMessage String userMessage);

    /**
     * 流式生成多文件代码
     *
     * @param userMessage 用户对网站的描述
     * @return 模型按 SSE/分块推送的字符串序列，完整拼接后等价于 {@link #generateMultiFileCode(String)}
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(@UserMessage String userMessage);


    /**
     * 生成 Vue 项目代码（流式）
     *
     * @param userMessage 用户消息
     * @return 生成过程的流式响应
     */
    @SystemMessage(fromResource = "prompt/codegen-vue-project-system-prompt.txt")
    TokenStream generateVueProjectCodeStream(@MemoryId long appId, @UserMessage String userMessage);

}
