package com.comonon.mall.user.vo;

import com.comonon.mall.user.entity.UserAddress;
import lombok.Data;

@Data
public class AddressVO {
    private Long id;
    private String receiver;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private String regionCode;
    private Integer isDefault;
    private String fullAddress;

    public static AddressVO from(UserAddress a) {
        AddressVO vo = new AddressVO();
        vo.setId(a.getId());
        vo.setReceiver(a.getReceiver());
        vo.setPhone(a.getPhone());
        vo.setProvince(a.getProvince());
        vo.setCity(a.getCity());
        vo.setDistrict(a.getDistrict());
        vo.setDetail(a.getDetail());
        vo.setRegionCode(a.getRegionCode());
        vo.setIsDefault(a.getIsDefault());
        vo.setFullAddress(a.getProvince() + a.getCity() + a.getDistrict() + a.getDetail());
        return vo;
    }
}
