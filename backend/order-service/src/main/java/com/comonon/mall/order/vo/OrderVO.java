package com.comonon.mall.order.vo;

import com.comonon.mall.order.entity.OrderEntity;
import com.comonon.mall.order.service.FulfillmentHelper;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {
    private String orderNo;
    private Long userId;
    /** 管理端展示用昵称 */
    private String userNickname;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal freightAmount;
    private BigDecimal payAmount;
    private Integer itemCount;
    private String productType;
    private String receiver;
    private String receiverPhone;
    private String addressDetail;
    private LocalDateTime expireAt;
    private LocalDateTime payAt;
    private LocalDateTime shipAt;
    private String trackingNo;
    private String trackingCompany;
    private LocalDateTime completeAt;
    private FulfillmentVO fulfillment;
    private LocalDateTime cancelAt;
    private String cancelReason;
    private String remark;
    private String adminRemark;
    private LocalDateTime refundAt;
    private String refundReason;
    private LocalDateTime verifiedAt;
    private PaymentBriefVO payment;
    private LocalDateTime createdAt;
    private List<OrderItemVO> items;
    private List<OrderLogVO> logs;

    public static OrderVO from(OrderEntity o, List<OrderItemVO> items) {
        OrderVO vo = new OrderVO();
        vo.setOrderNo(o.getOrderNo());
        vo.setUserId(o.getUserId());
        vo.setStatus(o.getStatus());
        vo.setTotalAmount(o.getTotalAmount());
        vo.setFreightAmount(o.getFreightAmount());
        vo.setPayAmount(o.getPayAmount());
        vo.setItemCount(o.getItemCount());
        vo.setProductType(o.getProductType());
        vo.setReceiver(o.getReceiver());
        vo.setReceiverPhone(o.getReceiverPhone());
        vo.setAddressDetail(o.getAddressDetail());
        vo.setExpireAt(o.getExpireAt());
        vo.setPayAt(o.getPayAt());
        vo.setShipAt(o.getShipAt());
        vo.setTrackingNo(o.getTrackingNo());
        vo.setTrackingCompany(o.getTrackingCompany());
        vo.setCompleteAt(o.getCompleteAt());
        vo.setFulfillment(FulfillmentHelper.fromJson(o.getFulfillmentJson()));
        vo.setCancelAt(o.getCancelAt());
        vo.setCancelReason(o.getCancelReason());
        vo.setRemark(o.getRemark());
        vo.setAdminRemark(o.getAdminRemark());
        vo.setRefundAt(o.getRefundAt());
        vo.setRefundReason(o.getRefundReason());
        vo.setVerifiedAt(o.getVerifiedAt());
        vo.setCreatedAt(o.getCreatedAt());
        vo.setItems(items);
        return vo;
    }
}
