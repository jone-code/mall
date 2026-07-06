package com.comonon.mall.cart.service;

import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.exception.BizException;
import com.comonon.mall.cart.client.ProductServiceClient;
import com.comonon.mall.cart.client.dto.SkuSnapshotClientDto;
import com.comonon.mall.cart.config.CartProperties;
import com.comonon.mall.cart.domain.CartLine;
import com.comonon.mall.cart.dto.AddCartItemRequest;
import com.comonon.mall.cart.dto.UpdateCartItemRequest;
import com.comonon.mall.cart.repository.CartRedisRepository;
import com.comonon.mall.cart.vo.AddCartResultVO;
import com.comonon.mall.cart.vo.CartItemVO;
import com.comonon.mall.cart.vo.CartListVO;
import com.comonon.mall.cart.vo.CartSummaryVO;
import com.comonon.mall.cart.vo.CheckoutPreviewVO;
import com.comonon.mall.cart.vo.InternalCartLineVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRedisRepository cartRepository;
    private final ProductServiceClient productClient;
    private final CartProperties cartProperties;

    public CartListVO list(Long userId) {
        return buildList(userId, cartRepository.findAll(userId));
    }

    public AddCartResultVO addItem(Long userId, AddCartItemRequest req) {
        SkuSnapshotClientDto snap = requireSellable(req.getSkuId());
        assertCartProductRules(userId, snap, req.getSkuId(), false);
        int reqQty = capQtyForType(snap, req.getQuantity());
        int maxQty = maxAllowed(snap.getAvailable(), snap.getProductType());
        CartLine existing = cartRepository.findLine(userId, req.getSkuId());
        int newQty;
        if (existing != null) {
            newQty = existing.getQuantity() + reqQty;
        } else {
            if (cartRepository.countLines(userId) >= cartProperties.getCart().getMaxLines()) {
                throw BizException.of(ErrorCode.CART_ITEM_LIMIT, "购物车商品种类已达上限");
            }
            newQty = reqQty;
        }
        newQty = capQtyForType(snap, newQty);
        if (newQty > maxQty) {
            throw BizException.of(ErrorCode.SKU_NOT_SELLABLE, "库存不足");
        }
        CartLine line = existing != null ? existing : cartRepository.newLine(req.getSkuId(), newQty, true);
        line.setQuantity(newQty);
        if (existing == null) {
            line.setSelected(true);
            line.setAddedAt(LocalDateTime.now());
        }
        cartRepository.saveLine(userId, line);
        AddCartResultVO vo = new AddCartResultVO();
        vo.setCartItemCount(sumQuantity(userId));
        vo.setSkuQuantity(newQty);
        return vo;
    }

    public void updateItem(Long userId, Long skuId, UpdateCartItemRequest req) {
        CartLine line = cartRepository.findLine(userId, skuId);
        if (line == null) {
            throw BizException.of(ErrorCode.CART_ITEM_NOT_FOUND, "购物车无此商品");
        }
        if (req.getQuantity() != null) {
            if (req.getQuantity() == 0) {
                cartRepository.deleteLine(userId, skuId);
                return;
            }
            SkuSnapshotClientDto snap = productClient.fetchSnapshots(List.of(skuId)).get(skuId);
            if (snap == null) {
                throw BizException.of(ErrorCode.SKU_NOT_FOUND, "SKU 不存在");
            }
            assertCartProductRules(userId, snap, skuId, true);
            int qty = capQtyForType(snap, req.getQuantity());
            int maxQty = maxAllowed(snap.getAvailable() == null ? 0 : snap.getAvailable(), snap.getProductType());
            if (qty > maxQty) {
                throw BizException.of(ErrorCode.CART_STOCK_INSUFFICIENT, "库存不足");
            }
            line.setQuantity(qty);
        }
        if (req.getSelected() != null) {
            line.setSelected(req.getSelected());
        }
        cartRepository.saveLine(userId, line);
    }

    public void selectAll(Long userId, boolean selected) {
        List<CartLine> lines = cartRepository.findAll(userId);
        for (CartLine line : lines) {
            line.setSelected(selected);
            cartRepository.saveLine(userId, line);
        }
    }

    public void deleteItem(Long userId, Long skuId) {
        cartRepository.deleteLine(userId, skuId);
    }

    public int deleteInvalid(Long userId) {
        CartListVO list = list(userId);
        int removed = 0;
        for (CartItemVO item : list.getItems()) {
            if (Boolean.TRUE.equals(item.getInvalid())) {
                cartRepository.deleteLine(userId, item.getSkuId());
                removed++;
            }
        }
        return removed;
    }

    public CheckoutPreviewVO checkoutPreview(Long userId) {
        CartListVO list = list(userId);
        List<CartItemVO> selected = list.getItems().stream()
                .filter(i -> Boolean.TRUE.equals(i.getSelected()))
                .filter(i -> !Boolean.TRUE.equals(i.getInvalid()))
                .toList();
        if (selected.isEmpty()) {
            throw BizException.of(ErrorCode.CART_NOTHING_SELECTED, "请先勾选商品");
        }
        for (CartItemVO item : selected) {
            if (Boolean.TRUE.equals(item.getStockInsufficient())) {
                throw BizException.of(ErrorCode.CART_STOCK_INSUFFICIENT, "部分商品库存不足");
            }
        }
        assertSelectedProductRules(selected);
        CheckoutPreviewVO vo = new CheckoutPreviewVO();
        vo.setItems(selected);
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (CartItemVO item : selected) {
            BigDecimal sub = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(sub);
            count += item.getQuantity();
        }
        vo.setTotalAmount(total);
        vo.setItemCount(count);
        vo.setSkuLineCount(selected.size());
        return vo;
    }

    /** 内部：取勾选项（skuId+quantity），供 order-service 建单。 */
    public List<InternalCartLineVO> selectedLines(Long userId) {
        return cartRepository.findAll(userId).stream()
                .filter(l -> Boolean.TRUE.equals(l.getSelected()))
                .map(l -> new InternalCartLineVO(l.getSkuId(), l.getQuantity()))
                .toList();
    }

    /** 内部：下单成功后移除已下单的行。 */
    public void removeLines(Long userId, List<Long> skuIds) {
        if (skuIds == null) {
            return;
        }
        for (Long skuId : skuIds) {
            cartRepository.deleteLine(userId, skuId);
        }
    }

    private SkuSnapshotClientDto requireSellable(Long skuId) {
        Map<Long, SkuSnapshotClientDto> map = productClient.fetchSnapshots(List.of(skuId));
        SkuSnapshotClientDto snap = map.get(skuId);
        if (snap == null) {
            throw BizException.of(ErrorCode.SKU_NOT_FOUND, "SKU 不存在");
        }
        if (!Boolean.TRUE.equals(snap.getSellable())) {
            throw BizException.of(ErrorCode.SKU_NOT_SELLABLE, "商品不可购买");
        }
        return snap;
    }

    private CartListVO buildList(Long userId, List<CartLine> lines) {
        List<Long> skuIds = lines.stream().map(CartLine::getSkuId).toList();
        Map<Long, SkuSnapshotClientDto> snapshots = productClient.fetchSnapshots(skuIds);
        List<CartItemVO> items = new ArrayList<>();
        CartSummaryVO summary = new CartSummaryVO();
        int totalQty = 0;
        int selectedQty = 0;
        BigDecimal selectedAmount = BigDecimal.ZERO;
        int invalidCount = 0;

        for (CartLine line : lines) {
            SkuSnapshotClientDto snap = snapshots.get(line.getSkuId());
            CartItemVO vo = new CartItemVO();
            vo.setSkuId(line.getSkuId());
            vo.setQuantity(line.getQuantity());
            vo.setSelected(line.getSelected());
            vo.setAddedAt(line.getAddedAt());
            if (snap != null) {
                vo.setSpuId(snap.getSpuId());
                vo.setTitle(snap.getSpuTitle());
                vo.setSpecText(snap.getSpecText());
                vo.setMainImage(snap.getMainImage());
                vo.setPrice(snap.getPrice());
                vo.setAvailable(snap.getAvailable());
                vo.setSellable(snap.getSellable());
                vo.setProductType(snap.getProductType());
                boolean invalid = !Boolean.TRUE.equals(snap.getSellable());
                vo.setInvalid(invalid);
                vo.setInvalidReason(snap.getInvalidReason());
                boolean insufficient = !invalid && line.getQuantity() > (snap.getAvailable() == null ? 0 : snap.getAvailable());
                vo.setStockInsufficient(insufficient);
                if (invalid) {
                    invalidCount++;
                    if (Boolean.TRUE.equals(line.getSelected())) {
                        line.setSelected(false);
                        cartRepository.saveLine(userId, line);
                        vo.setSelected(false);
                    }
                }
            } else {
                vo.setInvalid(true);
                vo.setInvalidReason("SKU_NOT_FOUND");
                invalidCount++;
            }
            items.add(vo);
            totalQty += line.getQuantity();
            if (Boolean.TRUE.equals(vo.getSelected()) && !Boolean.TRUE.equals(vo.getInvalid())
                    && vo.getPrice() != null) {
                selectedQty += line.getQuantity();
                selectedAmount = selectedAmount.add(vo.getPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
            }
        }
        summary.setTotalQuantity(totalQty);
        summary.setSelectedQuantity(selectedQty);
        summary.setSelectedAmount(selectedAmount);
        summary.setInvalidCount(invalidCount);
        CartListVO result = new CartListVO();
        result.setItems(items);
        result.setSummary(summary);
        return result;
    }

    private int maxAllowed(int available, String productType) {
        int cap = cartProperties.getCart().getMaxQuantityPerLine();
        if ("VIRTUAL".equals(productType) || "SERVICE".equals(productType)) {
            cap = 1;
        }
        return Math.min(available, cap);
    }

    private int capQtyForType(SkuSnapshotClientDto snap, int qty) {
        String type = normalizeType(snap.getProductType());
        if (("VIRTUAL".equals(type) || "SERVICE".equals(type)) && qty > 1) {
            throw BizException.of(ErrorCode.CART_VIRTUAL_QTY_LIMIT, "虚拟/服务类商品每单限购 1 件");
        }
        return qty;
    }

    private void assertCartProductRules(Long userId, SkuSnapshotClientDto snap, Long skuId, boolean updating) {
        String newType = normalizeType(snap.getProductType());
        List<CartLine> lines = cartRepository.findAll(userId);
        if (lines.isEmpty()) {
            return;
        }
        List<Long> otherSkuIds = lines.stream()
                .map(CartLine::getSkuId)
                .filter(id -> !id.equals(skuId))
                .toList();
        if (otherSkuIds.isEmpty()) {
            return;
        }
        Map<Long, SkuSnapshotClientDto> snapshots = productClient.fetchSnapshots(otherSkuIds);
        for (Long otherId : otherSkuIds) {
            SkuSnapshotClientDto other = snapshots.get(otherId);
            if (other == null) {
                continue;
            }
            String otherType = normalizeType(other.getProductType());
            if (!newType.equals(otherType)) {
                throw BizException.of(ErrorCode.CART_MIXED_PRODUCT_TYPE,
                        "购物车不可同时包含实物、虚拟、服务类商品");
            }
            if ("VIRTUAL".equals(newType) || "SERVICE".equals(newType)) {
                throw BizException.of(ErrorCode.BAD_REQUEST, "虚拟/服务类商品请单独购买");
            }
        }
        if (!updating && ("VIRTUAL".equals(newType) || "SERVICE".equals(newType))
                && lines.stream().anyMatch(l -> !l.getSkuId().equals(skuId))) {
            throw BizException.of(ErrorCode.BAD_REQUEST, "虚拟/服务类商品请单独购买");
        }
    }

    private void assertSelectedProductRules(List<CartItemVO> selected) {
        if (selected.isEmpty()) {
            return;
        }
        String type = normalizeType(selected.get(0).getProductType());
        for (CartItemVO item : selected) {
            if (!type.equals(normalizeType(item.getProductType()))) {
                throw BizException.of(ErrorCode.CART_MIXED_PRODUCT_TYPE,
                        "请分开结算实物、虚拟、服务类商品");
            }
        }
        if ("VIRTUAL".equals(type) || "SERVICE".equals(type)) {
            if (selected.size() > 1) {
                throw BizException.of(ErrorCode.BAD_REQUEST, "虚拟/服务类商品请单独购买");
            }
            if (selected.get(0).getQuantity() != null && selected.get(0).getQuantity() > 1) {
                throw BizException.of(ErrorCode.CART_VIRTUAL_QTY_LIMIT, "虚拟/服务类商品每单限购 1 件");
            }
        }
    }

    private static String normalizeType(String productType) {
        return productType == null ? "PHYSICAL" : productType;
    }

    private int sumQuantity(Long userId) {
        return cartRepository.findAll(userId).stream().mapToInt(CartLine::getQuantity).sum();
    }
}
