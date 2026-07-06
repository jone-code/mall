package com.comonon.mall.bff.exception;

import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.api.Result;
import com.comonon.mall.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * BFF 全局异常处理：所有异常统一封装成 {@link Result}（基于 common 的 ErrorCode 错误码体系）。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<Void>> handleBiz(BizException ex) {
        log.warn("BizException: code={}, msg={}", ex.getCode(), ex.getMessage());
        return ResponseEntity.status(mapStatus(ex.getCode()))
                .body(Result.error(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegal(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(Result.error(ErrorCode.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleAny(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(ErrorCode.INTERNAL_ERROR, "服务异常"));
    }

    private HttpStatus mapStatus(int code) {
        if (code >= 40100 && code < 40200) return HttpStatus.UNAUTHORIZED;
        if (code >= 40300 && code < 40400) return HttpStatus.FORBIDDEN;
        if (code >= 40000 && code < 40100) return HttpStatus.BAD_REQUEST;
        return HttpStatus.OK;
    }
}
