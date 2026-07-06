package com.comonon.mall.order.vo;

import com.comonon.mall.order.entity.OrderLogEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderLogVO {
    private Long id;
    private String fromStatus;
    private String toStatus;
    private String operatorType;
    private String operatorId;
    private String remark;
    private LocalDateTime createdAt;

    public static OrderLogVO from(OrderLogEntity e) {
        OrderLogVO vo = new OrderLogVO();
        vo.setId(e.getId());
        vo.setFromStatus(e.getFromStatus());
        vo.setToStatus(e.getToStatus());
        vo.setOperatorType(e.getOperatorType());
        vo.setOperatorId(e.getOperatorId());
        vo.setRemark(e.getRemark());
        vo.setCreatedAt(e.getCreatedAt());
        return vo;
    }
}
