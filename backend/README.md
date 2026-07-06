# mall-backend

ComonOn Mall 后端多模块工程。本期落地 `user-service`。

## 模块概览

- `common`: 通用 jar（无 web 依赖）。包含 `JwtUtil`、`Result<T>`、`ErrorCode`、`BizException` + `GlobalExceptionHandler`、`RedisKeys` 常量、`UserContext`/`UserPrincipal`。
- `user-service`: C 端用户服务（端口 `8101`）。手机号短信登录 + 微信登录、双 Token 会话管理、设备互踢、refresh rotation、JWT 黑名单、`/me` 资料、`/me/sessions` 设备管理。
- `admin-service`、`mall-bff`: 占位 jar，后续实现。

## 启动方式

### 1. 准备依赖

- JDK 21
- Maven 3.6+
- MySQL 8（端口 3306）
- Redis 7（端口 6379）

### 2. 建库 + 跑 schema

```sql
CREATE DATABASE mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

C 端表（`user`/`user_identity`）与管理端表（`admin_*`）均在同一库 `mall`：

```sh
mysql -uroot mall < mall/backend/sql/schema.sql
```

> 使用 `mall/deploy` Docker 时，`mysql-init.sql` 会在首启自动建库建表。重复执行等价于 IF NOT EXISTS。

### 3. 配置环境变量

```sh
export MYSQL_HOST=127.0.0.1
export MYSQL_USER=root
export MYSQL_PASSWORD=root
export REDIS_HOST=127.0.0.1
export MALL_JWT_SECRET=$(openssl rand -hex 32)
```

### 4. 编译 & 启动

```sh
cd mall/backend
mvn -pl common,user-service -am package -DskipTests
java -jar user-service/target/user-service-1.0.0-SNAPSHOT.jar
```

或使用 Maven 直接运行：

```sh
mvn -pl user-service -am spring-boot:run
```

### 5. 接口列表（user-service）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/sms/send` | 发送短信验证码（开发环境只 log） |
| POST | `/login/sms` | 验证码登录（含自动注册） |
| POST | `/oauth/wechat` | 微信登录（Stub：openid = `stub_openid_${code}`） |
| POST | `/token/refresh` | 刷新 access + 轮换 refresh |
| POST | `/logout` | 登出当前会话 |
| POST | `/logout/all` | 登出全部会话 |
| GET  | `/me` | 当前用户资料 |
| PUT  | `/me` | 更新昵称/头像 |
| GET  | `/me/sessions` | 列出当前用户活跃会话 |
| DELETE | `/me/sessions/{sid}` | 单独踢下某会话 |

## 测试

```sh
cd mall/backend
mvn -pl common,user-service -am test
```

## 替换 Stub

- 短信网关：实现 `com.comonon.mall.user.sms.SmsGateway` 替换 `SmsGatewayStub`。
- 微信登录：实现 `com.comonon.mall.user.oauth.WechatOAuthClient` 替换 `WechatOAuthStub`。
