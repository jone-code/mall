package com.comonon.mall.product.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SkuSnapshotListVO {
    private List<SkuSnapshotVO> items = new ArrayList<>();
}
