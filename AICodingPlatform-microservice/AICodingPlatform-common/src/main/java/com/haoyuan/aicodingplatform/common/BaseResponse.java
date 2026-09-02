package com.haoyuan.aicodingplatform.common;

import com.haoyuan.aicodingplatform.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 基础响应
 *
 * @param <T> 响应数据类型
 */

@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    public BaseResponse() {
        // Jackson 反序列化需要无参构造函数
    }

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
