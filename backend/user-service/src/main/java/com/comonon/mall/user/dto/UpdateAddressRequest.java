package com.comonon.mall.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateAddressRequest {
    @Size(min = 2, max = 32)
    private String receiver;
    private String phone;
    @Size(max = 32)
    private String province;
    @Size(max = 32)
    private String city;
    @Size(max = 32)
    private String district;
    @Size(min = 4, max = 256)
    private String detail;
    private String regionCode;
    private Boolean isDefault;
}
