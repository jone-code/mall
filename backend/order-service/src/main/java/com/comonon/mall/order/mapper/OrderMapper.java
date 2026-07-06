package com.comonon.mall.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.comonon.mall.order.entity.OrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {

    /** 各状态订单数与金额聚合，用于主控台。 */
    @Select("SELECT status AS status, COUNT(*) AS cnt FROM orders GROUP BY status")
    List<Map<String, Object>> aggregateByStatus();

    /** 指定时间起新增订单数（按下单时间）。 */
    @Select("SELECT COUNT(*) FROM orders WHERE created_at >= #{start}")
    long countCreatedSince(@Param("start") LocalDateTime start);

    /** 指定时间起已支付 GMV（按支付时间）。 */
    @Select("SELECT COALESCE(SUM(pay_amount),0) FROM orders "
            + "WHERE pay_at >= #{start} AND status IN ('PAID','SHIPPED','COMPLETED')")
    BigDecimal gmvPaidSince(@Param("start") LocalDateTime start);

    /** 累计已支付 GMV。 */
    @Select("SELECT COALESCE(SUM(pay_amount),0) FROM orders "
            + "WHERE status IN ('PAID','SHIPPED','COMPLETED')")
    BigDecimal gmvPaidTotal();

    /** 待支付 → 已支付（状态守卫，原子）。返回受影响行数。 */
    @Update("UPDATE orders SET status='PAID', pay_at=#{payAt}, updated_at=NOW() "
            + "WHERE order_no=#{orderNo} AND status='PENDING_PAY'")
    int markPaid(@Param("orderNo") String orderNo, @Param("payAt") LocalDateTime payAt);

    /** 待支付 → 已取消（状态守卫，原子）。返回受影响行数。 */
    @Update("UPDATE orders SET status='CANCELLED', cancel_at=#{cancelAt}, cancel_reason=#{reason}, updated_at=NOW() "
            + "WHERE order_no=#{orderNo} AND status='PENDING_PAY'")
    int markCancelled(@Param("orderNo") String orderNo,
                      @Param("reason") String reason,
                      @Param("cancelAt") LocalDateTime cancelAt);

    /** 已支付 → 已发货（实物）。 */
    @Update("UPDATE orders SET status='SHIPPED', ship_at=#{shipAt}, tracking_no=#{trackingNo}, "
            + "tracking_company=#{trackingCompany}, updated_at=NOW() "
            + "WHERE order_no=#{orderNo} AND status='PAID'")
    int markShipped(@Param("orderNo") String orderNo,
                    @Param("shipAt") LocalDateTime shipAt,
                    @Param("trackingNo") String trackingNo,
                    @Param("trackingCompany") String trackingCompany);

    /** 已支付 → 已完成（虚拟商品，带履约 JSON）。 */
    @Update("UPDATE orders SET status='COMPLETED', complete_at=#{completeAt}, fulfillment_json=#{fulfillmentJson}, "
            + "updated_at=NOW() WHERE order_no=#{orderNo} AND status='PAID'")
    int markVirtualCompleted(@Param("orderNo") String orderNo,
                             @Param("completeAt") LocalDateTime completeAt,
                             @Param("fulfillmentJson") String fulfillmentJson);

    /** 已支付 → 已发货（服务类，带核销码）。 */
    @Update("UPDATE orders SET status='SHIPPED', ship_at=#{shipAt}, fulfillment_json=#{fulfillmentJson}, "
            + "updated_at=NOW() WHERE order_no=#{orderNo} AND status='PAID'")
    int markServiceShipped(@Param("orderNo") String orderNo,
                           @Param("shipAt") LocalDateTime shipAt,
                           @Param("fulfillmentJson") String fulfillmentJson);

    /** 已发货 → 已完成。 */
    @Update("UPDATE orders SET status='COMPLETED', complete_at=#{completeAt}, updated_at=NOW() "
            + "WHERE order_no=#{orderNo} AND status='SHIPPED'")
    int markCompleted(@Param("orderNo") String orderNo, @Param("completeAt") LocalDateTime completeAt);

    /** 已支付/已发货/已完成 → 退款中。 */
    @Update("UPDATE orders SET status='REFUNDING', refund_reason=#{reason}, refund_from_status=#{fromStatus}, "
            + "updated_at=NOW() WHERE order_no=#{orderNo} AND status=#{fromStatus}")
    int markRefunding(@Param("orderNo") String orderNo,
                      @Param("reason") String reason,
                      @Param("fromStatus") String fromStatus);

    /** 退款中 → 已退款。 */
    @Update("UPDATE orders SET status='REFUNDED', refund_at=#{refundAt}, updated_at=NOW() "
            + "WHERE order_no=#{orderNo} AND status='REFUNDING'")
    int markRefunded(@Param("orderNo") String orderNo, @Param("refundAt") LocalDateTime refundAt);

    /** 退款中 → 恢复原状态。 */
    @Update("UPDATE orders SET status=#{restoreStatus}, refund_reason=NULL, refund_from_status=NULL, updated_at=NOW() "
            + "WHERE order_no=#{orderNo} AND status='REFUNDING'")
    int markRefundRejected(@Param("orderNo") String orderNo, @Param("restoreStatus") String restoreStatus);

    /** 服务核销：已发货 → 已完成。 */
    @Update("UPDATE orders SET status='COMPLETED', complete_at=#{completeAt}, verified_at=#{verifiedAt}, "
            + "updated_at=NOW() WHERE order_no=#{orderNo} AND status='SHIPPED' AND product_type='SERVICE'")
    int markServiceVerified(@Param("orderNo") String orderNo,
                            @Param("completeAt") LocalDateTime completeAt,
                            @Param("verifiedAt") LocalDateTime verifiedAt);

    @Update("UPDATE orders SET admin_remark=#{remark}, updated_at=NOW() WHERE order_no=#{orderNo}")
    int updateAdminRemark(@Param("orderNo") String orderNo, @Param("remark") String remark);

    @Select("SELECT DATE(created_at) AS day, COUNT(*) AS cnt FROM orders "
            + "WHERE created_at >= #{start} GROUP BY DATE(created_at) ORDER BY day")
    List<Map<String, Object>> countOrdersByDay(@Param("start") LocalDateTime start);

    @Select("SELECT DATE(pay_at) AS day, COALESCE(SUM(pay_amount),0) AS gmv FROM orders "
            + "WHERE pay_at >= #{start} AND status IN ('PAID','SHIPPED','COMPLETED') "
            + "GROUP BY DATE(pay_at) ORDER BY day")
    List<Map<String, Object>> gmvByDay(@Param("start") LocalDateTime start);

    @Select("SELECT * FROM orders WHERE product_type='SERVICE' AND status='SHIPPED' "
            + "AND JSON_UNQUOTE(JSON_EXTRACT(fulfillment_json,'$.verifyCode')) = #{code} LIMIT 1")
    OrderEntity findServiceByVerifyCode(@Param("code") String code);
}
