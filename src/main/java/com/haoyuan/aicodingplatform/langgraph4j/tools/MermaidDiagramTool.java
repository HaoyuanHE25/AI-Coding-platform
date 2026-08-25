package com.haoyuan.aicodingplatform.langgraph4j.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import com.haoyuan.aicodingplatform.exception.BusinessException;
import com.haoyuan.aicodingplatform.exception.ErrorCode;
import com.haoyuan.aicodingplatform.langgraph4j.model.ImageResource;
import com.haoyuan.aicodingplatform.langgraph4j.model.enums.ImageCategoryEnum;
import com.haoyuan.aicodingplatform.manager.CosManager;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MermaidDiagramTool {

    @Resource
    private CosManager cosManager;

    @Tool("将 Mermaid 代码转换为架构图图片，用于展示系统结构和技术关系")
    public List<ImageResource> generateMermaidDiagram(@P("Mermaid 图表代码") String mermaidCode,
                                                      @P("架构图描述") String description) {
        if (StrUtil.isBlank(mermaidCode)) {
            return new ArrayList<>();
        }
        try {
            // 转换为SVG图片
            File diagramFile = convertMermaidToSvg(mermaidCode);
            // 上传到COS
            String keyName = String.format("/mermaid/%s/%s",
                    RandomUtil.randomString(5), diagramFile.getName());
            String cosUrl = cosManager.uploadFile(keyName, diagramFile);
            // 清理临时文件
            FileUtil.del(diagramFile);
            if (StrUtil.isNotBlank(cosUrl)) {
                return Collections.singletonList(ImageResource.builder()
                        .category(ImageCategoryEnum.ARCHITECTURE)
                        .description(description)
                        .url(cosUrl)
                        .build());
            }
        } catch (Exception e) {
            log.error("生成架构图失败: {}", e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    /**
     * 将Mermaid代码转换为SVG图片 - 使用多种方式尝试
     */
    private File convertMermaidToSvg(String mermaidCode) {
        // 创建临时输入文件 - 使用UTF-8编码
        File tempInputFile = FileUtil.createTempFile("mermaid_input_", ".mmd", true);
        File tempOutputFile = FileUtil.createTempFile("mermaid_output_", ".svg", true);

        try {
            // 写入UTF-8编码
            Files.write(tempInputFile.toPath(),
                    mermaidCode.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            log.info("写入临时文件成功，大小: {} bytes", tempInputFile.length());

            // 尝试多种执行方式
            boolean success = false;
            String[] methods = {"npx", "mmdc", "fullpath"};

            for (String method : methods) {
                try {
                    if (executeMermaid(method, tempInputFile, tempOutputFile)) {
                        success = true;
                        break;
                    }
                } catch (Exception e) {
                    log.warn("方法 {} 执行失败: {}", method, e.getMessage());
                }
            }

            if (!success) {
                // 尝试使用在线API作为最后手段
                if (convertViaApi(mermaidCode, tempOutputFile)) {
                    success = true;
                }
            }

            if (!success || !tempOutputFile.exists() || tempOutputFile.length() == 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                        "所有Mermaid转换方式都失败，请确保Node.js已安装或网络连接正常");
            }

            log.info("Mermaid转换成功，输出文件大小: {} bytes", tempOutputFile.length());
            FileUtil.del(tempInputFile);
            return tempOutputFile;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Mermaid转换失败", e);
            FileUtil.del(tempInputFile);
            FileUtil.del(tempOutputFile);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Mermaid转换失败: " + e.getMessage());
        }
    }

    /**
     * 执行Mermaid转换
     */
    private boolean executeMermaid(String method, File inputFile, File outputFile) {
        try {
            String userHome = System.getProperty("user.home");
            String command;

            switch (method) {
                case "npx":
                    // 使用 npx（推荐）
                    command = String.format("npx @mermaid-js/mermaid-cli -i \"%s\" -o \"%s\" -b transparent",
                            inputFile.getAbsolutePath(),
                            outputFile.getAbsolutePath());
                    break;
                case "mmdc":
                    // 直接使用 mmdc
                    String cmd = SystemUtil.getOsInfo().isWindows() ? "mmdc.cmd" : "mmdc";
                    command = String.format("%s -i \"%s\" -o \"%s\" -b transparent",
                            cmd,
                            inputFile.getAbsolutePath(),
                            outputFile.getAbsolutePath());
                    break;
                case "fullpath":
                    // 使用完整路径
                    String mmdcPath = userHome + "\\AppData\\Roaming\\npm\\mmdc.cmd";
                    if (!new File(mmdcPath).exists()) {
                        mmdcPath = "C:\\Program Files\\nodejs\\node_modules\\@mermaid-js\\mermaid-cli\\src\\cli.js";
                    }
                    command = String.format("\"%s\" -i \"%s\" -o \"%s\" -b transparent",
                            mmdcPath,
                            inputFile.getAbsolutePath(),
                            outputFile.getAbsolutePath());
                    break;
                default:
                    return false;
            }

            log.info("尝试执行方法 {}: {}", method, command);

            // 使用 ProcessBuilder 执行
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
            pb.redirectErrorStream(true);

            // 设置环境变量
            Map<String, String> env = pb.environment();
            String path = env.get("PATH");
            if (path != null) {
                env.put("PATH", path + ";" + userHome + "\\AppData\\Roaming\\npm");
            }

            Process process = pb.start();

            // 读取输出
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("Mermaid-{}: {}", method, line);
                }
            }

            // 等待执行完成，设置30秒超时
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                log.warn("方法 {} 执行超时", method);
                return false;
            }

            int exitCode = process.exitValue();
            log.info("方法 {} 退出码: {}", method, exitCode);

            return exitCode == 0 && outputFile.exists() && outputFile.length() > 0;

        } catch (Exception e) {
            log.warn("方法 {} 执行异常: {}", method, e.getMessage());
            return false;
        }
    }

    /**
     * 使用Mermaid在线API转换
     */
    private boolean convertViaApi(String mermaidCode, File outputFile) {
        try {
            log.info("尝试使用在线API转换...");

            // 使用Mermaid的在线服务
            String encoded = java.net.URLEncoder.encode(mermaidCode, "UTF-8");
            String url = "https://mermaid.ink/svg/" + encoded;

            java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
                    new java.net.URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            if (conn.getResponseCode() == 200) {
                try (java.io.InputStream is = conn.getInputStream()) {
                    Files.copy(is, outputFile.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    log.info("在线API转换成功，文件大小: {} bytes", outputFile.length());
                    return true;
                }
            } else {
                log.warn("在线API返回错误状态码: {}", conn.getResponseCode());
            }
        } catch (Exception e) {
            log.warn("在线API转换失败: {}", e.getMessage());
        }
        return false;
    }
}