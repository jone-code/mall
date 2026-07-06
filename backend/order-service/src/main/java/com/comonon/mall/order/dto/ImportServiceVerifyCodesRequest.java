package com.comonon.mall.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ImportServiceVerifyCodesRequest {
    @NotNull(message = "请选择服务商品")
    private Long spuId;

    @NotEmpty(message = "核销码列表不能为空")
    @Valid
    private List<CodeItem> codes;

    @Data
    public static class CodeItem {
        private String verifyCode;
    }
}
