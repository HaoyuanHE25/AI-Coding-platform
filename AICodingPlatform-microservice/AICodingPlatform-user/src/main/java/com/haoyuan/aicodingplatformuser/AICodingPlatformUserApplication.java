package com.haoyuan.aicodingplatformuser;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.haoyuan.aicodingplatformuser.mapper")
// 显式限定扫描范围：本服务自身包 + 公共模块包，避免误扫到同名的历史遗留类
@ComponentScan({"com.haoyuan.aicodingplatformuser", "com.haoyuan.aicodingplatform"})
@EnableDubbo
public class AICodingPlatformUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(AICodingPlatformUserApplication.class, args);
    }
}
