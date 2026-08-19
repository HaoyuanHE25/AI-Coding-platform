package com.haoyuan.aicodingplatform.controller;

import com.haoyuan.aicodingplatform.constant.AppConstant;
import com.haoyuan.aicodingplatform.model.enums.CodeGenTypeEnum;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import java.io.File;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/static")
public class StaticResourceController {

    // 应用生成根目录（用于浏览）
    private static final String PREVIEW_ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;

    /**
     * 提供静态资源访问，支持目录重定向
     * 访问格式：http://localhost:8123/api/static/{deployKey}[/{fileName}]
     * <p>
     * 对于 Vue 项目等存在 dist 子目录的应用，自动从 dist 目录提供构建产物。
     */
    @GetMapping("/{deployKey}/**")
    public ResponseEntity<Resource> serveStaticResource(
            @PathVariable String deployKey,
            HttpServletRequest request) {
        try {
            // 获取资源路径
            String resourcePath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            resourcePath = resourcePath.substring(("/static/" + deployKey).length());
            // 如果是目录访问（不带斜杠），重定向到带斜杠的URL
            if (resourcePath.isEmpty()) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Location", request.getRequestURI() + "/");
                return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
            }
            // 默认返回 index.html
            if (resourcePath.equals("/")) {
                resourcePath = "/index.html";
            }
            // 构建基础目录：若存在 dist 子目录（Vue 项目构建产物），则从 dist 提供
            String baseDir = PREVIEW_ROOT_DIR + "/" + deployKey;
            File distDir = new File(baseDir, "dist");
            if (distDir.exists() && distDir.isDirectory()) {
                baseDir = distDir.getAbsolutePath();
            } else if (isVueProject(deployKey) && "/index.html".equals(resourcePath)) {
                // 未部署的 Vue 项目尚未构建完成（dist 不存在）：
                // 原始 index.html 通过绝对路径引用 /src/main.js，浏览器无法解析会导致预览空白。
                // 这里返回一个自动刷新的"构建中"占位页面，待异步构建完成 dist 出现后即可正常展示。
                return ResponseEntity.ok()
                        .header("Content-Type", "text/html; charset=UTF-8")
                        .body(buildingInProgressResource());
            }
            // 构建文件路径
            String filePath = baseDir + resourcePath;
            File file = new File(filePath);
            // 检查文件是否存在
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            // 返回文件资源
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header("Content-Type", getContentTypeWithCharset(filePath))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根据文件扩展名返回带字符编码的 Content-Type
     */
    private String getContentTypeWithCharset(String filePath) {
        if (filePath.endsWith(".html")) return "text/html; charset=UTF-8";
        if (filePath.endsWith(".css")) return "text/css; charset=UTF-8";
        if (filePath.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (filePath.endsWith(".png")) return "image/png";
        if (filePath.endsWith(".jpg")) return "image/jpeg";
        return "application/octet-stream";
    }

    /**
     * 判断 deployKey 是否对应 Vue 项目（未部署预览时 deployKey 形如 vue_project_{appId}）
     */
    private boolean isVueProject(String deployKey) {
        return deployKey != null
                && deployKey.startsWith(CodeGenTypeEnum.VUE_PROJECT.getValue() + "_");
    }

    /**
     * "构建中"占位页面：iframe 每 3 秒自动刷新，直到 Vue 项目异步构建完成、dist 目录出现后即可展示真实页面。
     */
    private Resource buildingInProgressResource() {
        String html = """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                <meta charset="UTF-8">
                <meta http-equiv="refresh" content="3">
                <title>构建中</title>
                <style>
                body{font-family:Arial,sans-serif;display:flex;flex-direction:column;justify-content:center;align-items:center;height:100vh;margin:0;background:#f5f7fa;color:#333}
                .spinner{width:44px;height:44px;border:4px solid #e6e6e6;border-top-color:#1677ff;border-radius:50%;animation:spin 1s linear infinite;margin-bottom:18px}
                @keyframes spin{to{transform:rotate(360deg)}}
                h3{margin:0 0 6px;font-size:18px}
                p{margin:0;color:#888;font-size:13px}
                </style>
                </head>
                <body>
                <div class="spinner"></div>
                <h3>正在构建项目...</h3>
                <p>构建完成后将自动展示，请稍候</p>
                </body>
                </html>
                """;
        return new ByteArrayResource(html.getBytes(StandardCharsets.UTF_8));
    }
}
