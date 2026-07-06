package com.comonon.mall.product.web;

import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.api.Result;
import com.comonon.mall.product.entity.Spu;
import com.comonon.mall.product.mapper.SpuMapper;
import com.comonon.mall.product.vo.SpuMetaVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/spu")
@RequiredArgsConstructor
public class InternalSpuController {

    private final SpuMapper spuMapper;

    @GetMapping("/{id}")
    public Result<SpuMetaVO> meta(@PathVariable Long id) {
        Spu spu = spuMapper.selectById(id);
        if (spu == null) {
            return Result.error(ErrorCode.SPU_NOT_FOUND, "商品不存在");
        }
        SpuMetaVO vo = new SpuMetaVO();
        vo.setId(spu.getId());
        vo.setTitle(spu.getTitle());
        vo.setProductType(spu.getProductType());
        vo.setStatus(spu.getStatus());
        return Result.ok(vo);
    }
}
