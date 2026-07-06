package com.comonon.mall.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.comonon.mall.pay.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {

    /** PENDING → SUCCESS（状态守卫，原子）。返回受影响行数。 */
    @Update("UPDATE payment SET status='SUCCESS', channel_txn=#{txn}, paid_at=#{paidAt}, updated_at=NOW() "
            + "WHERE pay_no=#{payNo} AND status='PENDING'")
    int markSuccess(@Param("payNo") String payNo,
                    @Param("txn") String txn,
                    @Param("paidAt") LocalDateTime paidAt);

    /** SUCCESS → REFUNDED（Mock 退款，幂等）。 */
    @Update("UPDATE payment SET status='REFUNDED', updated_at=NOW() "
            + "WHERE order_no=#{orderNo} AND status='SUCCESS'")
    int markRefundedByOrderNo(@Param("orderNo") String orderNo);
}
