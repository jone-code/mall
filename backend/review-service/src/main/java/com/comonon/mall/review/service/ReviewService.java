package com.comonon.mall.review.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.exception.BizException;
import com.comonon.mall.review.client.OrderClient;
import com.comonon.mall.review.client.dto.OrderBriefDto;
import com.comonon.mall.review.dto.CreateReviewRequest;
import com.comonon.mall.review.entity.ReviewEntity;
import com.comonon.mall.review.mapper.ReviewMapper;
import com.comonon.mall.review.vo.PageResult;
import com.comonon.mall.review.vo.ReviewSummaryVO;
import com.comonon.mall.review.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final String COMPLETED = "COMPLETED";

    private final ReviewMapper reviewMapper;
    private final OrderClient orderClient;

    public ReviewVO create(Long userId, CreateReviewRequest req, String nickname) {
        String orderNo = req.getOrderNo().trim();
        ReviewEntity exists = reviewMapper.selectOne(
                Wrappers.<ReviewEntity>lambdaQuery().eq(ReviewEntity::getOrderNo, orderNo));
        if (exists != null) {
            throw BizException.of(ErrorCode.REVIEW_ALREADY_EXISTS, "该订单已评价");
        }

        OrderBriefDto order = orderClient.getOrder(orderNo);
        if (order == null || !userId.equals(order.getUserId())) {
            throw BizException.of(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (!COMPLETED.equals(order.getStatus())) {
            throw BizException.of(ErrorCode.REVIEW_NOT_ALLOWED, "仅已完成订单可评价");
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw BizException.of(ErrorCode.REVIEW_NOT_ALLOWED, "订单无商品信息");
        }
        String content = req.getContent() == null ? "" : req.getContent().trim();
        if (!StringUtils.hasText(content)) {
            throw BizException.of(ErrorCode.BAD_REQUEST, "评价内容不能为空");
        }
        OrderBriefDto.OrderItemDto item = order.getItems().get(0);

        ReviewEntity entity = new ReviewEntity();
        entity.setOrderNo(orderNo);
        entity.setUserId(userId);
        entity.setSpuId(item.getSpuId());
        entity.setSkuId(item.getSkuId());
        entity.setRating(req.getRating());
        entity.setContent(content);
        entity.setUserNickname(StringUtils.hasText(nickname) ? nickname.trim() : "用户");
        entity.setStatus(ReviewEntity.VISIBLE);
        entity.setCreatedAt(LocalDateTime.now());
        reviewMapper.insert(entity);
        return ReviewVO.from(entity);
    }

    public ReviewVO getByOrderNo(String orderNo) {
        ReviewEntity e = reviewMapper.selectOne(
                Wrappers.<ReviewEntity>lambdaQuery().eq(ReviewEntity::getOrderNo, orderNo));
        return e == null ? null : ReviewVO.from(e);
    }

    public ReviewVO getByOrderNoForUser(Long userId, String orderNo) {
        OrderBriefDto order = orderClient.getOrder(orderNo);
        if (order == null || !userId.equals(order.getUserId())) {
            throw BizException.of(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }
        return getByOrderNo(orderNo);
    }

    public PageResult<ReviewVO> listBySpu(Long spuId, int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 50);
        var qw = Wrappers.<ReviewEntity>lambdaQuery()
                .eq(ReviewEntity::getSpuId, spuId)
                .eq(ReviewEntity::getStatus, ReviewEntity.VISIBLE)
                .orderByDesc(ReviewEntity::getId);
        long total = reviewMapper.selectCount(qw);
        qw.last("LIMIT " + ((p - 1) * s) + "," + s);
        List<ReviewVO> list = reviewMapper.selectList(qw).stream().map(ReviewVO::from).toList();
        return PageResult.of(list, p, s, total);
    }

    public ReviewSummaryVO summaryBySpu(Long spuId) {
        Map<String, Object> row = reviewMapper.aggregateBySpu(spuId);
        ReviewSummaryVO vo = new ReviewSummaryVO();
        if (row == null) {
            vo.setCount(0);
            vo.setAvgRating(BigDecimal.ZERO);
            return vo;
        }
        Object cnt = row.get("cnt");
        vo.setCount(cnt instanceof Number n ? n.longValue() : 0);
        Object avg = row.get("avgRating");
        if (avg instanceof BigDecimal bd) {
            vo.setAvgRating(bd.setScale(1, RoundingMode.HALF_UP));
        } else if (avg instanceof Number n) {
            vo.setAvgRating(BigDecimal.valueOf(n.doubleValue()).setScale(1, RoundingMode.HALF_UP));
        } else {
            vo.setAvgRating(BigDecimal.ZERO);
        }
        return vo;
    }

    public List<ReviewVO> listMine(Long userId) {
        return reviewMapper.selectList(Wrappers.<ReviewEntity>lambdaQuery()
                        .eq(ReviewEntity::getUserId, userId)
                        .orderByDesc(ReviewEntity::getId)
                        .last("LIMIT 100"))
                .stream().map(ReviewVO::from).toList();
    }

    public PageResult<ReviewVO> adminList(Long spuId, Integer rating, String status,
                                          LocalDate from, LocalDate to,
                                          int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 50);
        var qw = Wrappers.<ReviewEntity>lambdaQuery().orderByDesc(ReviewEntity::getId);
        if (spuId != null) {
            qw.eq(ReviewEntity::getSpuId, spuId);
        }
        if (rating != null) {
            qw.eq(ReviewEntity::getRating, rating);
        }
        if (StringUtils.hasText(status)) {
            qw.eq(ReviewEntity::getStatus, status.trim());
        }
        if (from != null) {
            qw.ge(ReviewEntity::getCreatedAt, from.atStartOfDay());
        }
        if (to != null) {
            qw.lt(ReviewEntity::getCreatedAt, to.plusDays(1).atStartOfDay());
        }
        long total = reviewMapper.selectCount(qw);
        qw.last("LIMIT " + ((p - 1) * s) + "," + s);
        List<ReviewVO> list = reviewMapper.selectList(qw).stream().map(ReviewVO::from).toList();
        return PageResult.of(list, p, s, total);
    }

    public void hide(Long id) {
        ReviewEntity e = reviewMapper.selectById(id);
        if (e == null) {
            throw BizException.of(ErrorCode.REVIEW_NOT_FOUND, "评价不存在");
        }
        e.setStatus(ReviewEntity.HIDDEN);
        reviewMapper.updateById(e);
    }

    public void unhide(Long id) {
        ReviewEntity e = reviewMapper.selectById(id);
        if (e == null) {
            throw BizException.of(ErrorCode.REVIEW_NOT_FOUND, "评价不存在");
        }
        e.setStatus(ReviewEntity.VISIBLE);
        reviewMapper.updateById(e);
    }
}
