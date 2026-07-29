package com.haoyuan.aicodingplatform.controller;

import com.haoyuan.aicodingplatform.common.BaseResponse;
import com.haoyuan.aicodingplatform.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/")
    public BaseResponse<String> healthCheck(){

        return ResultUtils.success("health ok");
    }



}
