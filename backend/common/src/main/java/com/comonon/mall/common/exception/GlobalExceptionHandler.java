package com.comonon.mall.common.exception;

import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.api.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<Void>> handleBiz(BizException e) {
        log.warn("biz exception code={} msg={}", e.getCode(), e.getMessage());
        HttpStatus status = mapStatus(e.getCode());
        return ResponseEntity.status(status).body(Result.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .orElse("参数校验失败");
        return ResponseEntity.badRequest().body(Result.error(ErrorCode.BAD_REQUEST, msg));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBind(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .orElse("参数校验失败");
        return ResponseEntity.badRequest().body(Result.error(ErrorCode.BAD_REQUEST, msg));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraint(ConstraintViolationException e) {
        return ResponseEntity.badRequest().body(Result.error(ErrorCode.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIAE(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Result.error(ErrorCode.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleAll(Exception e) {
        log.error("unhandled exception", e);
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
