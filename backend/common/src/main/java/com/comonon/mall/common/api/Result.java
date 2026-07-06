package com.comonon.mall.common.api;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应体：{ code, message, data, traceId }。
 */
@Data
@NoArgsConstructor
public class Result<T> implements Serializable {

    private int code;
    private String message;
    private T data;
    private String traceId;

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> ok() {
        return new Result<>(ErrorCode.OK, "ok", null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(ErrorCode.OK, "ok", data);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    public Result<T> withTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }
}
