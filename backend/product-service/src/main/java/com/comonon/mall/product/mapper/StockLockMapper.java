package com.comonon.mall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.comonon.mall.product.entity.StockLock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface StockLockMapper extends BaseMapper<StockLock> {

    @Select("SELECT DISTINCT order_no FROM stock_lock "
            + "WHERE status = 1 AND expire_at < #{now} LIMIT #{limit}")
    List<String> findExpiredLockedOrderNos(@Param("now") LocalDateTime now, @Param("limit") int limit);

    @Update("UPDATE stock_lock SET expire_at=#{expireAt}, updated_at=NOW() "
            + "WHERE order_no=#{orderNo} AND status=1")
    int extendExpireAt(@Param("orderNo") String orderNo, @Param("expireAt") LocalDateTime expireAt);
}
