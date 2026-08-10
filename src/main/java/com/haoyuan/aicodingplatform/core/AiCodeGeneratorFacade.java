package com.haoyuan.aicodingplatform.core;

import com.haoyuan.aicodingplatform.ai.AiCodeGeneratorService;
import com.haoyuan.aicodingplatform.core.parser.CodeParserExecutor;
import com.haoyuan.aicodingplatform.core.saver.CodeFileSaverExecutor;
import com.haoyuan.aicodingplatform.exception.BusinessException;
import com.haoyuan.aicodingplatform.exception.ErrorCode;
import com.haoyuan.aicodingplatform.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成外观类，组合生成、解析和保存功能。
 *
 * 统一处理流程（同步 / 流式共用）：
 *   AiCodeGeneratorService（返回原始文本 String / Flux<String>）
 *     → CodeParserExecutor（解析为 HtmlCodeResult / MultiFileCodeResult）
 *     → CodeFileSaverExecutor（落盘为文件）
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 通用流式代码处理方法：收集流 → 解析 → 保存
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType) {
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream.doOnNext(chunk -> {
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            try {
                String completeCode = codeBuilder.toString();
                Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType);
                log.info("保存成功，路径为：" + savedDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败: {}", e.getMessage());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码保存失败: " + e.getMessage());
            }
        });
    }

    /**
     * 统一入口：根据类型生成并保存代码（同步）
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> {
                String rawText = aiCodeGeneratorService.generateHtmlCode(userMessage);
                Object parsed = CodeParserExecutor.executeParser(rawText, CodeGenTypeEnum.HTML);
                yield CodeFileSaverExecutor.executeSaver(parsed, CodeGenTypeEnum.HTML);
            }
            case MULTI_FILE -> {
                String rawText = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                Object parsed = CodeParserExecutor.executeParser(rawText, CodeGenTypeEnum.MULTI_FILE);
                yield CodeFileSaverExecutor.executeSaver(parsed, CodeGenTypeEnum.MULTI_FILE);
            }
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                    "不支持的生成类型：" + codeGenTypeEnum.getValue());
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码（流式）
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> processCodeStream(
                    aiCodeGeneratorService.generateHtmlCodeStream(userMessage),
                    CodeGenTypeEnum.HTML);
            case MULTI_FILE -> processCodeStream(
                    aiCodeGeneratorService.generateMultiFileCodeStream(userMessage),
                    CodeGenTypeEnum.MULTI_FILE);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                    "不支持的生成类型：" + codeGenTypeEnum.getValue());
        };
    }
}