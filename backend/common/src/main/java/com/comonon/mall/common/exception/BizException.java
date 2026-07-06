package com.comonon.mall.common.exception;

import com.comonon.mall.common.api.ErrorCode;
import lombok.Getter;

/**
 * 业务异常。
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public static BizException of(int code, String message) {
        return new BizException(code, message);
    }

    public static BizException badRequest(String message) {
        return new BizException(ErrorCode.BAD_REQUEST, message);
    }
}
