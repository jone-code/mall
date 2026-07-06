# admin-web

ComonOn 电商平台 —— 管理后台前端基座（Vue3 + Vite + TypeScript + Pinia + Vue Router + Element Plus）。

实现两步登录、强制改密、权限基座（路由守卫 + `v-perm` 指令 + 菜单按权限渲染）。后端默认地址 `http://localhost:8102`，对应 `mall/backend/admin-service`。

## 启动

```bash
# 在 mall/frontend/admin-web 目录下
pnpm i
pnpm dev          # 启动开发服务器, 默认 http://localhost:5173
pnpm build        # 类型检查 + 生产构建, 输出到 dist/
pnpm preview      # 预览 dist
```

## 环境变量

| 变量 | 含义 | 示例 |
|------|------|------|
| `VITE_API_BASE` | 后端 admin-service 地址 | `http://localhost:8102` |
| `VITE_ENV` | 环境标，右上角 `EnvBadge` 显示 `TEST/PRE` | `test` |

`.env.development` 默认 `VITE_ENV=test`，生产构建建议设置为 `prod`（不显示角标）。

## 目录结构

```
src/
  api/http.ts          axios 封装：注入 Authorization；401 → /admin/token/refresh 重试一次；
                       403+KICKED → ElMessageBox 提示并跳 /login
  stores/admin.ts      Pinia store: accessToken/profile/permissions/permsVersion + login step1/2
  router/index.ts      路由守卫：未登录跳 /login；已登录访问 /login 跳 /dashboard；perm 不足跳 /no-permission
  directives/perm.ts   v-perm="'product:create'" 按权限隐藏元素
  components/
    CaptchaImage.vue   图形验证码（点击刷新）
    SmsCodeInput.vue   6 格短信验证码输入（自动跳格、粘贴、错误抖动）
    PasswordInput.vue  密码输入 + 强度条
    EnvBadge.vue       右上角环境标
  views/
    Login.vue          两步登录页 (step1: 账号/密码/图形验证码/7 天免登录；step2: 短信验证码 + 倒计时)
    ForcePwdReset.vue  强制改密：原密码/新密码/确认密码 + 强度条 + 规则提示
    Dashboard.vue      占位主控台，左侧菜单按 permissions 渲染
    NoPermission.vue   403 页
```

## 设计依据

- `.comate/specs/ecommerce-platform/auth-design.md` §11 管理后台登录方案
- `.comate/specs/ecommerce-platform/auth-ui-design.md` §3 管理后台 UI

## 接口契约（admin-service）

- `GET /admin/captcha` → `{ captchaId, image }`
- `POST /admin/login/password` → `{ challengeToken, phoneMasked }`
- `POST /admin/login/verify` → `{ accessToken, refreshToken?, profile, permissions, permsVersion }`
- `POST /admin/token/refresh` → `{ accessToken }`
- `POST /admin/logout`
- `GET /admin/me` → `{ profile, permissions, permsVersion }`
- `POST /admin/me/password`
