package com.comonon.mall.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.comonon.mall.review.entity.ReviewEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface ReviewMapper extends BaseMapper<ReviewEntity> {

    @Select("SELECT COUNT(*) AS cnt, COALESCE(AVG(rating),0) AS avgRating FROM order_reviews "
            + "WHERE spu_id=#{spuId} AND status='VISIBLE'")
    Map<String, Object> aggregateBySpu(@Param("spuId") Long spuId);

    @Select("SELECT COUNT(*) FROM order_reviews WHERE status='VISIBLE' AND rating <= 2")
    long countBadVisible();

    @Select("SELECT COUNT(*) FROM order_reviews WHERE status='VISIBLE'")
    long countVisible();
}
