package com.haoyuan.aicodingplatform.service;

public interface ScreenshotService {


    /**
     * 生成并上传网页截图
     *
     * @param webUrl 网页URL
     * @return 截图的访问URL，失败返回null
     */
    String generateAndUploadScreenshot(String webUrl);
}
