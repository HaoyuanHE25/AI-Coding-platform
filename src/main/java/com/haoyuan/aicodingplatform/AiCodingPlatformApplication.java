package com.haoyuan.aicodingplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.haoyuan.aicodingplatform.mapper")
public class AiCodingPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiCodingPlatformApplication.class, args);
	}

}
