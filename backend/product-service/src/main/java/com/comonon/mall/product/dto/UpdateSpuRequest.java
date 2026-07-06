package com.comonon.mall.product.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateSpuRequest {
    private Long categoryId;
    @Size(min = 2, max = 128)
    private String title;
    private String subtitle;
    private String mainImage;
    private List<String> images;
    private String detailHtml;
    private Integer sortOrder;
}
