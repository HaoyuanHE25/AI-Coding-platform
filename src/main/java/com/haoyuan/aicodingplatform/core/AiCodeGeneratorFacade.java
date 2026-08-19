package com.haoyuan.aicodingplatform.core;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.haoyuan.aicodingplatform.ai.AiCodeGeneratorService;
import com.haoyuan.aicodingplatform.ai.AiCodeGeneratorServiceFactory;
import com.haoyuan.aicodingplatform.ai.model.HtmlCodeResult;
import com.haoyuan.aicodingplatform.ai.model.MultiFileCodeResult;
import com.haoyuan.aicodingplatform.ai.model.message.AiResponseMessage;
import com.haoyuan.aicodingplatform.ai.model.message.ToolExecutedMessage;
import com.haoyuan.aicodingplatform.ai.model.message.ToolRequestMessage;
import com.haoyuan.aicodingplatform.core.parser.CodeParserExecutor;
import com.haoyuan.aicodingplatform.core.saver.CodeFileSaverExecutor;
import com.haoyuan.aicodingplatform.exception.BusinessException;
import com.haoyuan.aicodingplatform.exception.ErrorCode;
import com.haoyuan.aicodingplatform.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成外观类，组合生成、解析和保存功能。
 * <p>
 * 统一处理流程（同步 / 流式共用）：
 * AiCodeGeneratorService（返回原始文本 String / Flux<String>）
 * → CodeParserExecutor（解析为 HtmlCodeResult / MultiFileCodeResult）
 * → CodeFileSaverExecutor（落盘为文件）
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 通用流式代码处理方法：收集流 → 解析 → 保存（无代码则跳过保存）
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId) {
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream.doOnNext(chunk -> {
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            try {
                String completeCode = codeBuilder.toString();
                Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                if (hasValidCode(parsedResult)) {
                    File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType, appId);
                    log.info("保存成功，路径为：" + savedDir.getAbsolutePath());
                } else {
                    log.info("AI 响应不含有效代码，跳过保存，作为对话消息返回");
                }
            } catch (Exception e) {
                log.error("保存失败: {}", e.getMessage());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码保存失败: " + e.getMessage());
            }
        });
    }

    /**
     * 判断解析结果是否包含有效代码
     */
    private boolean hasValidCode(Object parsedResult) {
        if (parsedResult instanceof HtmlCodeResult html) {
            return StrUtil.isNotBlank(html.getHtmlCode());
        }
        if (parsedResult instanceof MultiFileCodeResult multi) {
            return StrUtil.isNotBlank(multi.getHtmlCode());
        }
        return false;
    }

    /**
     * 统一入口：根据类型生成并保存代码（同步，无代码则跳过保存）
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);

        return switch (codeGenTypeEnum) {
            case HTML -> {
                String rawText = aiCodeGeneratorService.generateHtmlCode(userMessage);
                Object parsed = CodeParserExecutor.executeParser(rawText, CodeGenTypeEnum.HTML);
                if (hasValidCode(parsed)) {
                    yield CodeFileSaverExecutor.executeSaver(parsed, CodeGenTypeEnum.HTML, appId);
                }
                log.info("AI 响应不含有效代码，跳过保存，作为对话消息返回");
                yield null;
            }
            case MULTI_FILE -> {
                String rawText = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                Object parsed = CodeParserExecutor.executeParser(rawText, CodeGenTypeEnum.MULTI_FILE);
                if (hasValidCode(parsed)) {
                    yield CodeFileSaverExecutor.executeSaver(parsed, CodeGenTypeEnum.MULTI_FILE, appId);
                }
                log.info("AI 响应不含有效代码，跳过保存，作为对话消息返回");
                yield null;
            }
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                    "不支持的生成类型：" + codeGenTypeEnum.getValue());
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码（流式）
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        // 从工厂获取对应的 AiCodeGeneratorService
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> processCodeStream(
                    aiCodeGeneratorService.generateHtmlCodeStream(userMessage),
                    CodeGenTypeEnum.HTML, appId
            );
            case MULTI_FILE -> processCodeStream(
                    aiCodeGeneratorService.generateMultiFileCodeStream(userMessage),
                    CodeGenTypeEnum.MULTI_FILE, appId
            );

            case VUE_PROJECT ->  processTokenStream(
                    aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage)
            );
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                    "不支持的生成类型：" + codeGenTypeEnum.getValue());
        };
    }



    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     *
     * @param tokenStream TokenStream 对象
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream) {
        return Flux.create(sink -> {
            tokenStream.onPartialResponse((String partialResponse) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .onPartialToolCall(partialToolCall -> {
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(partialToolCall);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse((ChatResponse response) -> {
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });
    }

}