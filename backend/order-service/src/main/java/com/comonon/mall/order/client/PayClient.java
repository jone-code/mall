package com.comonon.mall.order.client;

import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.exception.BizException;
import com.comonon.mall.order.vo.PaymentBriefVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class PayClient {

    private final WebClient payServiceWebClient;
    private final ObjectMapper objectMapper;

    public PayClient(WebClient payServiceWebClient, ObjectMapper objectMapper) {
        this.payServiceWebClient = payServiceWebClient;
        this.objectMapper = objectMapper;
    }

    public PaymentBriefVO getByOrderNo(String orderNo) {
        try {
            JsonNode root = payServiceWebClient.get()
                    .uri("/internal/pay/order/{orderNo}", orderNo)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            if (root == null || root.path("data").isMissingNode() || root.path("data").isNull()) {
                return null;
            }
            return objectMapper.treeToValue(root.path("data"), PaymentBriefVO.class);
        } catch (Exception e) {
            log.warn("fetch payment failed orderNo={}", orderNo, e);
            return null;
        }
    }

    public void mockRefund(String orderNo) {
        JsonNode root;
        try {
            root = payServiceWebClient.post()
                    .uri("/internal/pay/order/{orderNo}/refund", orderNo)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (Exception e) {
            log.warn("mock refund failed orderNo={}", orderNo, e);
            throw BizException.of(ErrorCode.INTERNAL_ERROR, "支付退款失败");
        }
        if (root == null || root.path("code").asInt(-1) != 0) {
            String msg = root == null ? "支付退款失败" : root.path("message").asText("支付退款失败");
            throw BizException.of(ErrorCode.INTERNAL_ERROR, msg);
        }
    }
}
