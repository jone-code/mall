package com.comonon.mall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.comonon.mall.product.entity.SkuStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SkuStockMapper extends BaseMapper<SkuStock> {

    /** 锁定：available 足够才成功（原子防超卖），返回受影响行数。 */
    @Update("UPDATE sku_stock SET available = available - #{qty}, frozen = frozen + #{qty}, "
            + "version = version + 1, updated_at = NOW() "
            + "WHERE sku_id = #{skuId} AND available >= #{qty}")
    int lock(@Param("skuId") Long skuId, @Param("qty") int qty);

    /** 释放：frozen 回补到 available。 */
    @Update("UPDATE sku_stock SET available = available + #{qty}, frozen = frozen - #{qty}, "
            + "version = version + 1, updated_at = NOW() "
            + "WHERE sku_id = #{skuId} AND frozen >= #{qty}")
    int release(@Param("skuId") Long skuId, @Param("qty") int qty);

    /** 扣减：支付成功，frozen 归零（available 已在 lock 时扣过）。 */
    @Update("UPDATE sku_stock SET frozen = frozen - #{qty}, "
            + "version = version + 1, updated_at = NOW() "
            + "WHERE sku_id = #{skuId} AND frozen >= #{qty}")
    int deduct(@Param("skuId") Long skuId, @Param("qty") int qty);

    /** 退款回补：已扣减库存加回 available。 */
    @Update("UPDATE sku_stock SET available = available + #{qty}, "
            + "version = version + 1, updated_at = NOW() "
            + "WHERE sku_id = #{skuId}")
    int restore(@Param("skuId") Long skuId, @Param("qty") int qty);

    @Select("SELECT COUNT(*) FROM sku_stock st "
            + "INNER JOIN sku sk ON sk.id = st.sku_id AND sk.status = 1 "
            + "INNER JOIN spu sp ON sp.id = sk.spu_id AND sp.status = 1 AND sp.product_type = 'PHYSICAL' "
            + "WHERE st.available < #{threshold}")
    long countLowStock(@Param("threshold") int threshold);
}
