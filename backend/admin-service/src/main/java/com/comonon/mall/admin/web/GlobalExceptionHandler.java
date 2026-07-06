package com.comonon.mall.admin.web;

import com.comonon.mall.common.web.BusinessException;
import com.comonon.mall.common.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handle(BusinessException e) {
        HttpStatus status = mapStatus(e.getCode());
        return ResponseEntity.status(status).body(Result.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handle(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse("invalid request");
        return ResponseEntity.badRequest().body(Result.error(40000, msg));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handle(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Result.error(40000, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handle(Exception e) {
        log.error("[admin-service] unhandled exception", e);
        return ResponseEntity.status(500).body(Result.error(50000, "internal error"));
    }

    private HttpStatus mapStatus(int code) {
        if (code == 40111 || code == 40110 || code == 40101 || code == 40102 || code == 40120
                || code == 40121 || code == 40122 || code == 40123) return HttpStatus.UNAUTHORIZED;
        if (code == 40301 || code == 40302 || code == 40330 || code == 40331) return HttpStatus.FORBIDDEN;
        if (code == 42301) return HttpStatus.LOCKED;
        if (code >= 40000 && code < 50000) return HttpStatus.BAD_REQUEST;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
