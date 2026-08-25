package com.haoyuan.aicodingplatform.langgraph4j.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.haoyuan.aicodingplatform.langgraph4j.model.ImageResource;
import com.haoyuan.aicodingplatform.langgraph4j.model.enums.ImageCategoryEnum;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class UndrawIllustrationTool {

    // 改为直接请求HTML页面
    private static final String UNDRAW_SEARCH_URL = "https://undraw.co/search/%s";

    @Tool("搜索插画图片，用于网站美化和装饰")
    public List<ImageResource> searchIllustrations(@P("搜索关键词") String query) {
        List<ImageResource> imageList = new ArrayList<>();
        int searchCount = 12;

        // 构建搜索URL
        String url = String.format(UNDRAW_SEARCH_URL, query);
        log.info("搜索插画：{}", url);

        try (HttpResponse response = HttpRequest.get(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .execute()) {

            if (!response.isOk()) {
                log.warn("请求失败，状态码：{}", response.getStatus());
                return imageList;
            }

            String html = response.body();

            // 1. 提取 __NEXT_DATA__ 中的JSON
            String jsonData = extractNextData(html);
            if (StrUtil.isBlank(jsonData)) {
                log.warn("未找到 __NEXT_DATA__ 数据");
                return imageList;
            }

            // 2. 解析JSON
            JSONObject root = JSONUtil.parseObj(jsonData);
            JSONObject props = root.getJSONObject("props");
            if (props == null) {
                return imageList;
            }

            JSONObject pageProps = props.getJSONObject("pageProps");
            if (pageProps == null) {
                return imageList;
            }

            JSONArray initialResults = pageProps.getJSONArray("initialResults");
            if (initialResults == null || initialResults.isEmpty()) {
                log.info("关键词 '{}' 没有搜索结果", query);
                return imageList;
            }

            // 3. 提取图片数据
            int actualCount = Math.min(searchCount, initialResults.size());
            for (int i = 0; i < actualCount; i++) {
                JSONObject illustration = initialResults.getJSONObject(i);
                String title = illustration.getStr("title", "插画");
                String media = illustration.getStr("media", "");


                if (StrUtil.isNotBlank(media)) {
                    imageList.add(ImageResource.builder()
                            .category(ImageCategoryEnum.ILLUSTRATION)
                            .description(title)
                            .url(media)

                            .build());
                }
            }

            log.info("成功获取 {} 张插画", imageList.size());

        } catch (Exception e) {
            log.error("搜索插画失败：{}", e.getMessage(), e);
        }

        return imageList;
    }

    /**
     * 从HTML中提取 __NEXT_DATA__ 的JSON内容
     */
    private String extractNextData(String html) {
        String startTag = "<script id=\"__NEXT_DATA__\" type=\"application/json\">";
        String endTag = "</script>";

        int start = html.indexOf(startTag);
        if (start == -1) {
            return null;
        }

        start += startTag.length();
        int end = html.indexOf(endTag, start);
        if (end == -1) {
            return null;
        }

        return html.substring(start, end).trim();
    }
}