package com.comonon.mall.admin.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class AuditBodySanitizer {

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "oldPassword", "newPassword", "cardSecret", "captcha",
            "smsCode", "refreshToken", "accessToken", "challengeToken"
    );

    private static final Set<String> TARGET_ID_PARAMS = Set.of("orderNo", "id");

    private AuditBodySanitizer() {}

    public static String sanitizeArgs(ProceedingJoinPoint pjp, ObjectMapper mapper) {
        List<Object> safe = new ArrayList<>();
        for (Object arg : pjp.getArgs()) {
            if (arg == null) {
                continue;
            }
            if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) {
                continue;
            }
            if (arg instanceof byte[] bytes) {
                safe.add("[binary:" + bytes.length + " bytes]");
                continue;
            }
            safe.add(arg);
        }
        try {
            JsonNode node = mapper.valueToTree(safe);
            redactNode(node);
            String json = mapper.writeValueAsString(node);
            return json.length() > 4000 ? json.substring(0, 4000) : json;
        } catch (Exception e) {
            return "[unserializable]";
        }
    }

    public static String extractTargetId(ProceedingJoinPoint pjp) {
        if (!(pjp.getSignature() instanceof MethodSignature sig)) {
            return null;
        }
        String[] names = sig.getParameterNames();
        Object[] args = pjp.getArgs();
        if (names == null || args == null) {
            return null;
        }
        for (int i = 0; i < names.length && i < args.length; i++) {
            if (TARGET_ID_PARAMS.contains(names[i]) && args[i] != null) {
                return String.valueOf(args[i]);
            }
        }
        return null;
    }

    private static void redactNode(JsonNode node) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            obj.fieldNames().forEachRemaining(field -> {
                JsonNode child = obj.get(field);
                if (SENSITIVE_KEYS.contains(field)) {
                    obj.put(field, "[REDACTED]");
                } else {
                    redactNode(child);
                }
            });
        } else if (node.isArray()) {
            ArrayNode arr = (ArrayNode) node;
            for (int i = 0; i < arr.size(); i++) {
                redactNode(arr.get(i));
            }
        }
    }
}
