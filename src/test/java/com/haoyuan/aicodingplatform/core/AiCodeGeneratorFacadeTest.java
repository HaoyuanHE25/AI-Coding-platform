package com.haoyuan.aicodingplatform.core;

import com.haoyuan.aicodingplatform.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveCode() {
        File dir = aiCodeGeneratorFacade.generateAndSaveCode("生成一个登陆页面，不超20行代码", CodeGenTypeEnum.HTML, 1L);
        assertNotNull(dir);
        assertTrue(dir.isDirectory(), "返回的路径应该是一个目录");
        File[] files = dir.listFiles();
        assertNotNull(files);
        assertTrue(files.length >= 1, "HTML 模式应至少生成 index.html 文件");
        // 验证文件内容非空
        File indexHtml = new File(dir, "index.html");
        assertTrue(indexHtml.exists(), "index.html 文件应存在");
        assertTrue(indexHtml.length() > 0, "index.html 文件内容应非空");
    }

    @Test
    void generateAndSaveCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(
                "任务管理网站：支持添加、删除、标记完成任务，响应式设计",
                CodeGenTypeEnum.MULTI_FILE, 1L);
        List<String> chunks = codeStream.collectList().block();
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty(), "流式结果不应为空");
        String completeContent = String.join("", chunks);
        assertNotNull(completeContent);
        assertFalse(completeContent.isBlank(), "拼接后的完整内容不应为空白");

        // 验证保存目录下有三个文件
        File saveRoot = new File(System.getProperty("user.dir") + "/tmp/code_output");
        assertTrue(saveRoot.isDirectory(), "代码输出根目录应存在");
        File[] subDirs = saveRoot.listFiles();
        assertNotNull(subDirs);
        // 取最新的一个子目录(文件名按时间排序或任意一个 MULTI_FILE 目录)
        File latestMultiDir = null;
        for (File sub : subDirs) {
            if (sub.isDirectory() && sub.getName().startsWith("multi_file_")) {
                if (latestMultiDir == null || sub.lastModified() > latestMultiDir.lastModified()) {
                    latestMultiDir = sub;
                }
            }
        }
        assertNotNull(latestMultiDir, "应存在 multi_file_* 格式的保存目录");

        File indexHtml = new File(latestMultiDir, "index.html");
        File styleCss = new File(latestMultiDir, "style.css");
        File scriptJs = new File(latestMultiDir, "script.js");

        assertTrue(indexHtml.exists(), "index.html 文件应存在");
        assertTrue(styleCss.exists(), "style.css 文件应存在");
        assertTrue(scriptJs.exists(), "script.js 文件应存在");
        assertTrue(indexHtml.length() > 0, "index.html 文件内容应非空");
        assertTrue(styleCss.length() > 0, "style.css 文件内容应非空");
        assertTrue(scriptJs.length() > 0, "script.js 文件内容应非空");

        System.out.println("=== 保存目录内容 ===");
        System.out.println("目录: " + latestMultiDir.getAbsolutePath());
        System.out.println("index.html: " + indexHtml.length() + " bytes");
        System.out.println("style.css:  " + styleCss.length() + " bytes");
        System.out.println("script.js:  " + scriptJs.length() + " bytes");
    }

    @Test
    void generateVueProjectCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(
                "简单的任务记录网站，总代码量不超过 200 行",
                CodeGenTypeEnum.VUE_PROJECT, 1L);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }

}
