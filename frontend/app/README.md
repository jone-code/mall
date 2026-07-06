# ComonOn Mall App (uni-app + Vue3 + Vite + Pinia + TypeScript)

C 端电商 App，覆盖商品浏览、购物车、下单支付（Mock）、订单履约、评价与账号管理。

## Quickstart

```bash
pnpm i

# H5（默认 http://localhost:5174，API 代理到 BFF :8001）
pnpm dev:h5
pnpm build:h5

# 微信小程序
pnpm dev:mp-weixin
pnpm build:mp-weixin
```

本地依赖：`mall/deploy` 启动 MySQL/Redis，BFF `:8001`，各微服务按 `mall/docs/roadmap.md` 启动。

## 环境变量（可选）

| 变量 | 说明 |
|------|------|
| `VITE_API_BASE` | API 根路径，默认 `/api`；真机调试可设 `http://<host>:8001/api` |
| `VITE_FILE_BASE` | 静态文件根，默认走 Vite `/files` 代理 |
| `VITE_WECHAT_APPID` | H5 微信 OAuth AppID（未配置时微信登录会提示） |

## 目录结构

```
src/
├── api/                   # 认证、商品、购物车、订单、评价
├── components/            # CopyText、RelativeTime、VerifyQrCode、骨架屏等
├── composables/           # useCountdown 等
├── pages/
│   ├── index/             # 首页
│   ├── category/          # 分类
│   ├── cart/              # 购物车
│   ├── mine/              # 我的、资料、登录设备、协议
│   ├── order/             # 确认/支付/结果/列表/详情/评价
│   ├── product/           # 商品详情
│   ├── address/           # 收货地址
│   └── login/             # 登录注册
├── stores/                # user、cart
└── utils/                 # request、media、error-message、datetime
```

## 网络层约定

- baseURL：`/api`（开发走 Vite 代理 → BFF `localhost:8001`）
- 图片：`/files/**` 由 BFF 或 Vite 代理提供；`resolveMediaUrl()` 统一拼接
- 上传：`POST /api/upload`（multipart），用于头像等
- 自动注入 `Authorization: Bearer <accessToken>`
- access 剩余 < 5min 时主动 `/token/refresh`；401 重试一次后跳登录

## 设计依据

- `.comate/specs/ecommerce-platform/auth-design.md`
- `.comate/specs/ecommerce-platform/app-ui-design.md`
