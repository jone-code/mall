package com.comonon.mall.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sku")
public class Sku {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long spuId;
    private String skuCode;
    /** JSON 字符串 */
    private String specJson;
    private String specText;
    private BigDecimal price;
    private BigDecimal marketPrice;
    private Integer isDefault;
    /** 0 禁用 / 1 启用 */
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
