package com.comonon.mall.order.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 读取 mall.user 表昵称，供管理端订单列表展示（与 admin-service 会员列表同源）。
 */
@Mapper
public interface UserNicknameMapper {

    @Select({
            "<script>",
            "SELECT id, nickname FROM user",
            "WHERE id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    List<Map<String, Object>> selectNicknamesByIds(@Param("ids") Collection<Long> ids);
}
