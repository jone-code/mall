package com.comonon.mall.common.web;

import lombok.Getter;

/**
 * 业务异常。错误码定义见 auth-design.md §9。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
