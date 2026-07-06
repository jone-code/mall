package com.comonon.mall.order.client;

import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.exception.BizException;
import com.comonon.mall.order.client.dto.AddressSnapshotDto;
import com.comonon.mall.order.client.dto.UserBriefDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UserClient {

    private final WebClient userServiceWebClient;
    private final ObjectMapper objectMapper;

    public UserClient(WebClient userServiceWebClient, ObjectMapper objectMapper) {
        this.userServiceWebClient = userServiceWebClient;
        this.objectMapper = objectMapper;
    }

    public AddressSnapshotDto getAddress(Long addressId, Long userId) {
        JsonNode root;
        try {
            root = userServiceWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/internal/addresses/{id}")
                            .queryParam("userId", userId).build(addressId))
                    .retrieve().bodyToMono(JsonNode.class).block();
        } catch (Exception e) {
            log.warn("user address call failed: {}", e.getMessage());
            throw BizException.of(ErrorCode.INTERNAL_ERROR, "用户服务不可用");
        }
        if (root == null || root.path("code").asInt(-1) != 0) {
            throw BizException.of(ErrorCode.ADDRESS_NOT_FOUND, "地址不存在");
        }
        try {
            return objectMapper.treeToValue(root.path("data"), AddressSnapshotDto.class);
        } catch (Exception e) {
            log.warn("user address parse failed addressId={} userId={}: {}", addressId, userId, e.getMessage());
            throw BizException.of(ErrorCode.ADDRESS_NOT_FOUND, "地址不存在");
        }
    }

    public Map<Long, String> fetchNicknames(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        JsonNode root;
        try {
            root = userServiceWebClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path("/internal/users/brief");
                        for (Long id : userIds) {
                            builder.queryParam("ids", id);
                        }
                        return builder.build();
                    })
                    .retrieve().bodyToMono(JsonNode.class).block();
        } catch (Exception e) {
            log.warn("user brief call failed: {}", e.getMessage());
            return Collections.emptyMap();
        }
        if (root == null || root.path("code").asInt(-1) != 0) {
            return Collections.emptyMap();
        }
        try {
            var type = objectMapper.getTypeFactory().constructCollectionType(List.class, UserBriefDto.class);
            List<UserBriefDto> list = objectMapper.convertValue(root.path("data"), type);
            return list.stream()
                    .filter(u -> u.getId() != null)
                    .collect(Collectors.toMap(UserBriefDto::getId, UserBriefDto::getNickname, (a, b) -> a));
        } catch (Exception e) {
            log.warn("user brief parse failed: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}
