package com.comonon.mall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.comonon.mall.product.entity.Sku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface SkuMapper extends BaseMapper<Sku> {

    @Select("""
            SELECT MIN(price) FROM sku
            WHERE spu_id = #{spuId} AND status = 1
            """)
    BigDecimal minPrice(@Param("spuId") Long spuId);

    @Select("""
            SELECT MAX(price) FROM sku
            WHERE spu_id = #{spuId} AND status = 1
            """)
    BigDecimal maxPrice(@Param("spuId") Long spuId);
}
