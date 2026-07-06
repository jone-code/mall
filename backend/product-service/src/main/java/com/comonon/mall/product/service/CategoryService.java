package com.comonon.mall.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.exception.BizException;
import com.comonon.mall.product.dto.CreateCategoryRequest;
import com.comonon.mall.product.dto.UpdateCategoryRequest;
import com.comonon.mall.product.entity.Category;
import com.comonon.mall.product.entity.Spu;
import com.comonon.mall.product.mapper.CategoryMapper;
import com.comonon.mall.product.mapper.SpuMapper;
import com.comonon.mall.product.vo.CategoryTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final SpuMapper spuMapper;

    public List<CategoryTreeVO> treeForConsumer() {
        return buildTree(listEnabled());
    }

    public List<CategoryTreeVO> treeForAdmin() {
        return buildTree(categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSortOrder)
                .orderByAsc(Category::getId)));
    }

    private List<Category> listEnabled() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder)
                .orderByAsc(Category::getId));
    }

    private List<CategoryTreeVO> buildTree(List<Category> all) {
        Map<Long, List<Category>> byParent = all.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() == null ? 0L : c.getParentId()));
        return buildChildren(byParent, 0L);
    }

    private List<CategoryTreeVO> buildChildren(Map<Long, List<Category>> byParent, Long parentId) {
        List<Category> children = byParent.getOrDefault(parentId, List.of()).stream()
                .sorted(Comparator.comparing(Category::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(Category::getId))
                .toList();
        List<CategoryTreeVO> result = new ArrayList<>();
        for (Category c : children) {
            CategoryTreeVO vo = toTreeVo(c);
            vo.setChildren(buildChildren(byParent, c.getId()));
            result.add(vo);
        }
        return result;
    }

    @Transactional
    public Long create(CreateCategoryRequest req) {
        long parentId = req.getParentId() == null ? 0L : req.getParentId();
        int level = 1;
        if (parentId > 0) {
            Category parent = requireCategory(parentId);
            level = parent.getLevel() + 1;
            if (level > 3) {
                throw BizException.of(ErrorCode.BAD_REQUEST, "类目最多三级");
            }
        }
        Category cat = new Category();
        cat.setParentId(parentId);
        cat.setName(req.getName().trim());
        cat.setIconUrl(req.getIconUrl());
        cat.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        cat.setStatus(1);
        cat.setLevel(level);
        LocalDateTime now = LocalDateTime.now();
        cat.setCreatedAt(now);
        cat.setUpdatedAt(now);
        categoryMapper.insert(cat);
        return cat.getId();
    }

    @Transactional
    public void update(Long id, UpdateCategoryRequest req) {
        Category cat = requireCategory(id);
        if (req.getName() != null) {
            cat.setName(req.getName().trim());
        }
        if (req.getIconUrl() != null) {
            cat.setIconUrl(req.getIconUrl());
        }
        if (req.getSortOrder() != null) {
            cat.setSortOrder(req.getSortOrder());
        }
        if (req.getStatus() != null) {
            cat.setStatus(req.getStatus());
        }
        cat.setUpdatedAt(LocalDateTime.now());
        categoryMapper.updateById(cat);
    }

    @Transactional
    public void delete(Long id) {
        requireCategory(id);
        long childCount = categoryMapper.selectCount(new LambdaQueryWrapper<Category>().eq(Category::getParentId, id));
        if (childCount > 0) {
            throw BizException.of(ErrorCode.CATEGORY_HAS_CHILDREN, "类目下有子节点，不可删除");
        }
        long spuCount = spuMapper.selectCount(new LambdaQueryWrapper<Spu>().eq(Spu::getCategoryId, id));
        if (spuCount > 0) {
            throw BizException.of(ErrorCode.CATEGORY_HAS_SPU, "类目下有商品，不可删除");
        }
        categoryMapper.deleteById(id);
    }

    public Category requireCategory(Long id) {
        Category cat = categoryMapper.selectById(id);
        if (cat == null) {
            throw BizException.of(ErrorCode.CATEGORY_NOT_FOUND, "类目不存在");
        }
        return cat;
    }

    public Category requireEnabledLevel2(Long categoryId) {
        Category cat = requireCategory(categoryId);
        if (cat.getStatus() == null || cat.getStatus() != 1) {
            throw BizException.of(ErrorCode.BAD_REQUEST, "类目已禁用");
        }
        if (cat.getLevel() == null || cat.getLevel() != 2) {
            throw BizException.of(ErrorCode.BAD_REQUEST, "商品须挂在二级类目");
        }
        return cat;
    }

    /** 解析 C 端 categoryId：一级则展开其下二级 id 列表 */
    private CategoryTreeVO toTreeVo(Category c) {
        CategoryTreeVO vo = new CategoryTreeVO();
        vo.setId(c.getId());
        vo.setParentId(c.getParentId());
        vo.setName(c.getName());
        vo.setIconUrl(c.getIconUrl());
        vo.setSortOrder(c.getSortOrder());
        vo.setStatus(c.getStatus());
        vo.setLevel(c.getLevel());
        return vo;
    }

    public List<Long> resolveCategoryIdsForQuery(Long categoryId) {
        if (categoryId == null) {
            return List.of();
        }
        Category cat = categoryMapper.selectById(categoryId);
        if (cat == null || cat.getStatus() != 1) {
            return List.of(-1L);
        }
        if (cat.getLevel() != null && cat.getLevel() == 2) {
            return List.of(cat.getId());
        }
        if (cat.getLevel() != null && cat.getLevel() == 1) {
            return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                            .eq(Category::getParentId, cat.getId())
                            .eq(Category::getStatus, 1))
                    .stream().map(Category::getId).toList();
        }
        return List.of(-1L);
    }
}
