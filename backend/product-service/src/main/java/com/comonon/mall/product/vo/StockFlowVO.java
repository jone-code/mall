package com.comonon.mall.product.vo;

import com.comonon.mall.product.entity.StockFlow;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StockFlowVO {
    private Long id;
    private Long skuId;
    private String orderNo;
    private String changeType;
    private Integer deltaAvailable;
    private Integer deltaFrozen;
    private Integer availableAfter;
    private Integer frozenAfter;
    private String operatorType;
    private String operatorId;
    private String remark;
    private LocalDateTime createdAt;

    public static StockFlowVO from(StockFlow f) {
        StockFlowVO vo = new StockFlowVO();
        vo.setId(f.getId());
        vo.setSkuId(f.getSkuId());
        vo.setOrderNo(f.getOrderNo());
        vo.setChangeType(f.getChangeType());
        vo.setDeltaAvailable(f.getDeltaAvailable());
        vo.setDeltaFrozen(f.getDeltaFrozen());
        vo.setAvailableAfter(f.getAvailableAfter());
        vo.setFrozenAfter(f.getFrozenAfter());
        vo.setOperatorType(f.getOperatorType());
        vo.setOperatorId(f.getOperatorId());
        vo.setRemark(f.getRemark());
        vo.setCreatedAt(f.getCreatedAt());
        return vo;
    }
}
