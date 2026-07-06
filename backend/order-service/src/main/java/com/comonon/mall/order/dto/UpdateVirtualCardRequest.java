package com.comonon.mall.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateVirtualCardRequest {
    @NotNull(message = "请选择虚拟商品")
    private Long spuId;

    @NotBlank(message = "卡号不能为空")
    private String cardNo;

    /** 留空表示不修改卡密 */
    private String cardSecret;
}
