package com.comonon.mall.order.client.dto;

import lombok.Data;

@Data
public class AddressSnapshotDto {
    private Long id;
    private String receiver;
    private String phone;
    private String fullAddress;
}
