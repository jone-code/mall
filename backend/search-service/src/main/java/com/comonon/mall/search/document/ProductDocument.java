package com.comonon.mall.search.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;

@Data
@Document(indexName = "mall_products")
public class ProductDocument {
    @Id
    private Long id;
    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;
    @Field(type = FieldType.Text, analyzer = "standard")
    private String subtitle;
    @Field(type = FieldType.Long)
    private Long categoryId;
    @Field(type = FieldType.Keyword)
    private String productType;
    @Field(type = FieldType.Keyword)
    private String mainImage;
    @Field(type = FieldType.Double)
    private BigDecimal minPrice;
    @Field(type = FieldType.Double)
    private BigDecimal maxPrice;
    @Field(type = FieldType.Integer)
    private Integer sortOrder;
}
