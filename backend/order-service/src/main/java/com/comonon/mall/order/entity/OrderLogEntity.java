package com.comonon.mall.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_log")
public class OrderLogEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private String fromStatus;
    private String toStatus;
    private String operatorType;
    private String operatorId;
    private String remark;
    private LocalDateTime createdAt;
}
