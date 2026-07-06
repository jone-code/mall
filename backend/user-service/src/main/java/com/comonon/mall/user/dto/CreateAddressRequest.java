package com.comonon.mall.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAddressRequest {
    @NotBlank
    @Size(min = 2, max = 32)
    private String receiver;
    @NotBlank
    private String phone;
    @NotBlank
    @Size(max = 32)
    private String province;
    @NotBlank
    @Size(max = 32)
    private String city;
    @NotBlank
    @Size(max = 32)
    private String district;
    @NotBlank
    @Size(min = 4, max = 256)
    private String detail;
    private String regionCode;
    private Boolean isDefault;
}
