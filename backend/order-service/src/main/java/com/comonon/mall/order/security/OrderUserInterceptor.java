package com.comonon.mall.order.security;

import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.api.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * C 端订单接口鉴权：信任 BFF 注入的 X-User-Id。
 */
@Component
public class OrderUserInterceptor implements HandlerInterceptor {

    public static final String HEADER_USER_ID = "X-User-Id";

    private final ObjectMapper objectMapper;

    public OrderUserInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String raw = request.getHeader(HEADER_USER_ID);
        if (!StringUtils.hasText(raw)) {
            writeUnauthorized(response);
            return false;
        }
        try {
            OrderUserContext.setUserId(Long.parseLong(raw.trim()));
            return true;
        } catch (NumberFormatException e) {
            writeUnauthorized(response);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        OrderUserContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(
                Result.error(ErrorCode.UNAUTHORIZED, "缺少用户身份")));
    }
}
