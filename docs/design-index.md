# Mall 设计文档索引

Phase 1 商品模块已实现；Phase 2 地址与购物车设计已定稿，待评审后开发。

## Phase 0 用户（已完成）

| 文档 | 说明 |
|------|------|
| [doc.md](../../.comate/specs/ecommerce-platform/doc.md) | 总体架构 |
| [auth-design.md](../../.comate/specs/ecommerce-platform/auth-design.md) | 登录态、用户表 |
| [auth-ui-design.md](../../.comate/specs/ecommerce-platform/auth-ui-design.md) | 登录 UI + 设计图 Prompt |
| [auth-ui.pen](../../.comate/specs/ecommerce-platform/auth-ui.pen) | 登录交互稿 |

## Phase 1 商品（已实现）

| 顺序 | 文档 | 内容 |
|------|------|------|
| 1 | [product-design.md](../../.comate/specs/ecommerce-platform/product-design.md) | 领域模型、状态机、API |
| 2 | [inventory-design.md](../../.comate/specs/ecommerce-platform/inventory-design.md) | 库存、lock/release（Phase 3） |
| 3 | [product-ui-design.md](../../.comate/specs/ecommerce-platform/product-ui-design.md) | App + 后台 UI |
| | [app-ui-design.md](../../.comate/specs/ecommerce-platform/app-ui-design.md) | C 端全站 UI 索引 + 订单/我的 Prompt |
| | [product-ui.pen](../../.comate/specs/ecommerce-platform/product-ui.pen) | C 端商品三屏 Pencil 稿 |
| | `product-ui-app-*.png` | C 端视觉 Mockup（同目录） |

## Phase 2 地址与购物车（设计已定稿 → 待开发）

| 顺序 | 文档 | 内容 |
|------|------|------|
| 1 | [address-design.md](../../.comate/specs/ecommerce-platform/address-design.md) | 收货地址表、CRUD、默认地址 |
| 2 | [cart-design.md](../../.comate/specs/ecommerce-platform/cart-design.md) | Redis 购物车、加购、结算预览 |
| 3 | [cart-address-ui-design.md](../../.comate/specs/ecommerce-platform/cart-address-ui-design.md) | 购物车 Tab、地址页、加购交互 |

## Phase 3 订单 / Phase 4 支付（设计已定稿 → 开发中）

| 顺序 | 文档 | 内容 |
|------|------|------|
| 1 | [order-design.md](../../.comate/specs/ecommerce-platform/order-design.md) | 订单状态机、建单/取消/超时、库存联动 |
| 2 | [pay-design.md](../../.comate/specs/ecommerce-platform/pay-design.md) | 支付单、Mock 回调、幂等 |

## 开发计划

[roadmap.md](./roadmap.md)

## 设计 → 开发检查清单

### Phase 1（商品）

- [x] 三份文档评审
- [x] DDL + product-service
- [x] admin-web 类目/商品
- [x] app 首页/分类/详情

### Phase 2（地址+购物车）

- [x] 三份 Phase 2 文档
- [ ] DDL `user_address` 已执行（需本地 MySQL 空间）
- [x] product-service `/internal/sku/snapshot`
- [x] user-service 地址 API + cart-service + BFF 代理
- [x] app 购物车/地址/加购
