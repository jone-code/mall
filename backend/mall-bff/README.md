# mall-bff

C 端聚合层 BFF（Backend-For-Frontend），承载 App / H5 / 小程序进入电商后端的入口。

## 职责

- JWT 鉴权拦截（验签 → exp → 黑名单 `jwt:bl:{jti}`）
- 注入下游身份头 `X-User-Id`、`X-Device-Type`
- `/api/user/**` 透传给 user-service
- 聚合常用入口：`/api/me`、`/api/sms/send`、`/api/login/sms`、`/api/oauth/wechat`、`/api/token/refresh`、`/api/logout`

设计依据：
- `.comate/specs/ecommerce-platform/auth-design.md` §8
- `.comate/specs/ecommerce-platform/doc.md`

## 端口与依赖

| 项 | 默认值 | 说明 |
| ---- | ---- | ---- |
| `server.port` | 8001 | BFF 服务端口 |
| `mall.bff.services.user` | http://localhost:8101 | user-service 基础地址 |
| Redis | localhost:6379 | 校验 `jwt:bl:{jti}` 黑名单 |
| `MALL_JWT_SECRET` | 见 `application.yml` | JWT HS256 签名密钥（≥ 32 字节） |

## 启动

```bash
# 1. 编译
cd mall/backend
mvn -pl mall-bff -am compile

# 2. 运行
export MALL_JWT_SECRET=your-32bytes-secret-here-please-change
export REDIS_HOST=localhost
export USER_SERVICE_URL=http://localhost:8101
mvn -pl mall-bff -am spring-boot:run
```

或打成可执行 jar：

```bash
mvn -pl mall-bff -am package
java -jar mall-bff/target/mall-bff-1.0.0-SNAPSHOT.jar
```

## 鉴权放行白名单

在 `application.yml` 的 `mall.bff.auth.whitelist` 中配置，默认包含：

- `/api/sms/send`
- `/api/login/sms`
- `/api/oauth/wechat`
- `/api/token/refresh`
- `/api/captcha/verify`

## 测试

```bash
mvn -pl mall-bff -am test
```

`AuthInterceptorTest` 通过 Mockito 模拟 Redis 与 JwtUtil，覆盖：白名单、缺 Authorization、非法 JWT、黑名单命中、合法 token 注入身份头。
