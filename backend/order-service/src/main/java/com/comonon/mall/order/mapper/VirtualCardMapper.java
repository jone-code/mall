package com.comonon.mall.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.comonon.mall.order.entity.VirtualCardEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface VirtualCardMapper extends BaseMapper<VirtualCardEntity> {

    @Select("SELECT * FROM virtual_cards WHERE status='AVAILABLE' "
            + "AND spu_id=#{spuId} ORDER BY id ASC LIMIT 1 FOR UPDATE")
    VirtualCardEntity lockOneAvailable(@Param("spuId") Long spuId);

    @Select("SELECT * FROM virtual_cards WHERE order_no=#{orderNo} AND status='RESERVED' "
            + "LIMIT 1 FOR UPDATE")
    VirtualCardEntity findReservedByOrderNo(@Param("orderNo") String orderNo);

    @Update("UPDATE virtual_cards SET status='RESERVED', order_no=#{orderNo} "
            + "WHERE id=#{id} AND status='AVAILABLE'")
    int markReserved(@Param("id") Long id, @Param("orderNo") String orderNo);

    @Update("UPDATE virtual_cards SET status='ISSUED', order_no=#{orderNo}, issued_at=#{issuedAt} "
            + "WHERE id=#{id} AND status='AVAILABLE'")
    int markIssued(@Param("id") Long id, @Param("orderNo") String orderNo,
                   @Param("issuedAt") LocalDateTime issuedAt);

    @Update("UPDATE virtual_cards SET status='ISSUED', issued_at=#{issuedAt} "
            + "WHERE id=#{id} AND status='RESERVED' AND order_no=#{orderNo}")
    int markIssuedFromReserved(@Param("id") Long id, @Param("orderNo") String orderNo,
                               @Param("issuedAt") LocalDateTime issuedAt);

    @Select("SELECT COUNT(*) FROM virtual_cards WHERE status='AVAILABLE'")
    long countAvailable();

    @Select("SELECT spu_id AS spuId, "
            + "SUM(CASE WHEN status='AVAILABLE' THEN 1 ELSE 0 END) AS available, "
            + "SUM(CASE WHEN status='ISSUED' THEN 1 ELSE 0 END) AS issued, "
            + "COUNT(*) AS total FROM virtual_cards GROUP BY spu_id ORDER BY spu_id")
    List<java.util.Map<String, Object>> poolSummary();

    @Select("SELECT COUNT(*) FROM ("
            + "SELECT spu_id FROM virtual_cards GROUP BY spu_id "
            + "HAVING SUM(CASE WHEN status='AVAILABLE' THEN 1 ELSE 0 END) = 0"
            + ") t")
    long countEmptyPools();

    @Update("UPDATE virtual_cards SET status='AVAILABLE', order_no=NULL, issued_at=NULL "
            + "WHERE order_no=#{orderNo} AND status='RESERVED'")
    int releaseReservation(@Param("orderNo") String orderNo);

    @Update("UPDATE virtual_cards SET status='AVAILABLE', order_no=NULL, issued_at=NULL "
            + "WHERE order_no=#{orderNo} AND status='ISSUED'")
    int markAvailableByOrderNo(@Param("orderNo") String orderNo);

    @Update("UPDATE virtual_cards SET status='REVOKED' "
            + "WHERE order_no=#{orderNo} AND status='ISSUED'")
    int markRevokedByOrderNo(@Param("orderNo") String orderNo);
}
