package com.comonon.mall.order.client;

import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.exception.BizException;
import com.comonon.mall.order.client.dto.SkuSnapshotDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ProductClient {

    private final WebClient productServiceWebClient;
    private final ObjectMapper objectMapper;

    public ProductClient(WebClient productServiceWebClient, ObjectMapper objectMapper) {
        this.productServiceWebClient = productServiceWebClient;
        this.objectMapper = objectMapper;
    }

    public Map<Long, SkuSnapshotDto> fetchSnapshots(List<Long> skuIds) {
        Map<Long, SkuSnapshotDto> map = new HashMap<>();
        if (skuIds == null || skuIds.isEmpty()) {
            return map;
        }
        JsonNode root = post("/internal/sku/snapshot", Map.of("skuIds", skuIds));
        JsonNode items = root.path("data").path("items");
        if (items.isArray()) {
            for (JsonNode node : items) {
                try {
                    SkuSnapshotDto dto = objectMapper.treeToValue(node, SkuSnapshotDto.class);
                    if (dto.getSkuId() != null) {
                        map.put(dto.getSkuId(), dto);
                    }
                } catch (Exception e) {
                    log.warn("parse snapshot node failed: {}", e.getMessage());
                }
            }
        }
        return map;
    }

    public void lock(String orderNo, List<Map<String, Object>> items, int ttlSeconds) {
        Map<String, Object> body = new HashMap<>();
        body.put("orderNo", orderNo);
        body.put("items", items);
        body.put("ttlSeconds", ttlSeconds);
        requireOk(post("/internal/stock/lock", body));
    }

    public void release(String orderNo, String reason) {
        requireOk(post("/internal/stock/release", Map.of("orderNo", orderNo, "reason", reason == null ? "" : reason)));
    }

    public void deduct(String orderNo) {
        requireOk(post("/internal/stock/deduct", Map.of("orderNo", orderNo, "reason", "deduct")));
    }

    /** 将 SPU 下 SKU 可售库存同步为码池余量。 */
    public void syncSpuPoolStock(Long spuId, int available) {
        try {
            requireOk(post("/internal/stock/sync-spu-pool", Map.of("spuId", spuId, "available", available)));
        } catch (Exception e) {
            log.warn("sync spu pool stock failed spuId={} available={}: {}", spuId, available, e.getMessage());
        }
    }

    public void refundRestore(String orderNo) {
        requireOk(post("/internal/stock/refund-restore", Map.of("orderNo", orderNo, "reason", "REFUND")));
    }

    /** 支付成功后延长库存锁有效期，避免待支付 TTL 到期误释放。 */
    public void extendLockForPaid(String orderNo) {
        try {
            requireOk(post("/internal/stock/extend-lock", Map.of("orderNo", orderNo, "reason", "PAID")));
        } catch (Exception e) {
            log.warn("extend stock lock failed orderNo={}: {}", orderNo, e.getMessage());
        }
    }

    /** 校验 SPU 存在且为虚拟商品。 */
    public void assertVirtualSpu(Long spuId) {
        assertSpuType(spuId, "VIRTUAL", ErrorCode.VIRTUAL_CARD_SPU_INVALID, "仅可绑定虚拟商品");
    }

    /** 校验 SPU 存在且为服务商品。 */
    public void assertServiceSpu(Long spuId) {
        assertSpuType(spuId, "SERVICE", ErrorCode.SERVICE_VERIFY_SPU_INVALID, "仅可绑定服务商品");
    }

    private void assertSpuType(Long spuId, String expectedType, int errorCode, String wrongTypeMsg) {
        try {
            JsonNode root = productServiceWebClient.get()
                    .uri("/internal/spu/{id}", spuId)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            if (root == null || root.path("code").asInt(-1) != 0) {
                throw BizException.of(ErrorCode.SPU_NOT_FOUND, "商品不存在");
            }
            JsonNode data = root.path("data");
            if (!expectedType.equals(data.path("productType").asText())) {
                throw BizException.of(errorCode, wrongTypeMsg);
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.warn("fetch spu meta failed id={}", spuId, e);
            throw BizException.of(ErrorCode.INTERNAL_ERROR, "商品服务不可用");
        }
    }

    private JsonNode post(String uri, Object body) {
        try {
            return productServiceWebClient.post().uri(uri)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.warn("product call {} failed: {}", uri, e.getMessage());
            throw BizException.of(ErrorCode.INTERNAL_ERROR, "商品服务不可用");
        }
    }

    private void requireOk(JsonNode root) {
        if (root == null) {
            throw BizException.of(ErrorCode.INTERNAL_ERROR, "商品服务无响应");
        }
        int code = root.path("code").asInt(-1);
        if (code != 0) {
            String msg = root.path("message").asText("库存操作失败");
            throw BizException.of(code > 0 ? code : ErrorCode.INTERNAL_ERROR, msg);
        }
    }
}
