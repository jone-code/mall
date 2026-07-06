package com.comonon.mall.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.comonon.mall.admin.entity.MemberUser;
import com.comonon.mall.admin.vo.MemberAddressVO;
import com.comonon.mall.admin.vo.MemberUserVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MemberUserMapper extends BaseMapper<MemberUser> {

    @Select({
            "<script>",
            "SELECT u.id, u.nickname, u.avatar_url AS avatarUrl, u.status,",
            "       u.created_at AS createdAt, u.updated_at AS updatedAt,",
            "       ph.identifier AS phone",
            "FROM user u",
            "LEFT JOIN user_identity ph ON ph.user_id = u.id AND ph.identity_type = 'PHONE'",
            "<where>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND (u.nickname LIKE CONCAT('%', #{keyword}, '%')",
            "         OR ph.identifier LIKE CONCAT('%', #{keyword}, '%'))",
            "  </if>",
            "  <if test='status != null'>",
            "    AND u.status = #{status}",
            "  </if>",
            "</where>",
            "ORDER BY u.id DESC",
            "LIMIT #{offset}, #{limit}",
            "</script>"
    })
    List<MemberUserVO> selectMemberPage(@Param("keyword") String keyword,
                                        @Param("status") Integer status,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);

    @Select({
            "<script>",
            "SELECT COUNT(*) FROM user u",
            "LEFT JOIN user_identity ph ON ph.user_id = u.id AND ph.identity_type = 'PHONE'",
            "<where>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND (u.nickname LIKE CONCAT('%', #{keyword}, '%')",
            "         OR ph.identifier LIKE CONCAT('%', #{keyword}, '%'))",
            "  </if>",
            "  <if test='status != null'>",
            "    AND u.status = #{status}",
            "  </if>",
            "</where>",
            "</script>"
    })
    long countMembers(@Param("keyword") String keyword, @Param("status") Integer status);

    @Select({
            "SELECT u.id, u.nickname, u.avatar_url AS avatarUrl, u.status,",
            "       u.created_at AS createdAt, u.updated_at AS updatedAt,",
            "       ph.identifier AS phone",
            "FROM user u",
            "LEFT JOIN user_identity ph ON ph.user_id = u.id AND ph.identity_type = 'PHONE'",
            "WHERE u.id = #{id}"
    })
    MemberUserVO selectMemberById(@Param("id") Long id);

    @Select({
            "SELECT id, receiver, phone, province, city, district, detail, is_default AS isDefault",
            "FROM user_address WHERE user_id = #{userId} ORDER BY is_default DESC, id DESC"
    })
    List<MemberAddressVO> selectAddressesByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM user WHERE created_at >= #{start}")
    long countCreatedSince(@Param("start") java.time.LocalDateTime start);
}
