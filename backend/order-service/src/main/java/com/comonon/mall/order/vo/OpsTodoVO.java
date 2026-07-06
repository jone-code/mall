package com.comonon.mall.order.vo;

import lombok.Data;

/**
 * 运营待办统计（主控台）。
 */
@Data
public class OpsTodoVO {
    private long pendingPay;
    private long pendingShip;
    private long refunding;
    /** 卡密池可用总数 */
    private long virtualCardAvailable;
    /** 至少导入过卡密但当前无可用卡密的虚拟商品数 */
    private long virtualPoolEmpty;
    /** 至少导入过核销码但当前无可用码的服务商品数 */
    private long servicePoolEmpty;
}
