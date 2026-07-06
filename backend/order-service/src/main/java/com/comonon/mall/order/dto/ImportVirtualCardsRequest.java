package com.comonon.mall.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ImportVirtualCardsRequest {
    @NotNull(message = "请选择虚拟商品")
    private Long spuId;

    @NotEmpty(message = "卡密列表不能为空")
    @Valid
    private List<CardItem> cards;

    @Data
    public static class CardItem {
        private String cardNo;
        private String cardSecret;
    }
}
