# Mall Deploy

只用 Docker 跑中间件（MySQL + Redis），Java 服务在宿主机直接 `mvn spring-boot:run`。

## 启动中间件

```bash
cd mall/deploy
./start.sh up        # MySQL + Redis（首启执行 mysql-init.sql：建库 + 默认 admin）
./start.sh ps
./start.sh logs
./start.sh down       # 停
./start.sh clean      # 停 + 清数据卷（旧 mall_user/mall_admin 数据会删除）
./start.sh reset      # clean + 重新 up（单库 mall 首启初始化）
```

| 中间件 | 宿主机端口 | 容器端口 | 说明                                                |
| ------ | ---------- | -------- | --------------------------------------------------- |
| MySQL  | 13306      | 3306     | root/root，db: `mall`（C 端 + 管理端表同库，避开本机 3306 上已有的 mysql-test） |
| Redis  | 16379      | 6379     | admin db=1，user db=0                               |

默认管理员: `admin` / `Admin@12345`

## 只启动管理后台（最小依赖）

Dashboard / 商品管理 / 订单管理 需要 **admin-service + product-service + order-service** 同时在跑：

```bash
cd mall/deploy
./start.sh up              # MySQL + Redis（若未启动）
./admin-backend.sh         # :8102（前台，占一个终端）
./product-backend.sh       # :8103（后台）
./order-backend.sh         # :8105（后台）
./admin-frontend.sh        # :5173（前台，占一个终端）

# 或一条命令起全部 Java 服务（含上述三个 + user/cart/pay/bff）
./run-services.sh up
```

若 Dashboard 报 `order-service unavailable`，说明 **8105 未启动**，执行 `./order-backend.sh` 后刷新页面即可。

三个终端各起一个：

```bash
cd mall/backend
export MALL_JWT_SECRET=dev_secret_please_change_to_a_long_random_string_32bytes
export MALL_ADMIN_JWT_SECRET=dev_admin_secret_please_change_32bytes_long_random

mvn -pl user-service  -am spring-boot:run    # :8101
mvn -pl admin-service -am spring-boot:run    # :8102
mvn -pl mall-bff      -am spring-boot:run    # :8001
```

或一键后台启动（日志写到 `mall/deploy/logs/*.log`）：

```bash
cd mall/deploy
./run-services.sh up        # 三个服务后台启动
./run-services.sh status
./run-services.sh down
tail -f logs/user-service.log
```

## 客户端模拟

需要 `jq`：`brew install jq`

```bash
../scripts/sim-client.sh   # C 端：发短信 → /api/login/sms → /me → 刷新 → 登出
../scripts/sim-admin.sh    # 管理端：图形码 → /admin/login/password → /admin/login/verify → /admin/me → 登出
```

脚本从 `mall/deploy/logs/*.log` 抓 STUB 短信验证码；如未自动抓到会提示手动输入。

## 从双库迁移到单库 mall

若之前跑过 `mall_user` / `mall_admin`，MySQL 数据卷里仍是旧库，需要**清卷重建**（开发环境可接受丢数据时）：

```bash
cd mall/deploy
./run-services.sh down    # 先停 Java
./start.sh reset          # 删容器+数据卷，按 mysql-init.sql 建 mall
./run-services.sh up
```

仅停容器、**不删数据**用 `./start.sh down`；删数据卷用 `clean` 或 `reset`。

## 注意

- JWT 密钥仅供本地联调，不要带到生产。
- 短信/微信均为 STUB，验证码打印到 spring-boot 控制台或 `logs/*.log`。
- 数据库表结构由 `mysql-init.sql` 在 MySQL 首次初始化时建好；手工建库可参考 `backend/sql/schema.sql`。

## 完整业务表初始化（首启 docker 后若缺表）

Docker 首启仅执行 `mysql-init.sql`（用户 + 管理端 + 权限种子）。商品/订单/支付等表需按需执行 patch（顺序建议）。

**中文数据**：执行 SQL 时请始终加 `--default-character-set=utf8mb4`，否则 seed/patch 里的中文会变成乱码（双重 UTF-8 编码）。

```bash
cd mall/deploy
MYSQL="mysql --default-character-set=utf8mb4 -h127.0.0.1 -P13306 -uroot -proot mall"
$MYSQL < patch-product-tables.sql
$MYSQL < patch-user-address.sql
$MYSQL < patch-order-pay-tables.sql
$MYSQL < patch-payment-unique.sql
$MYSQL < patch-admin-permissions.sql
$MYSQL < patch-orders-stats-index.sql
# 可选演示数据
$MYSQL < seed-products.sql
# 若已有乱码，修正种子与权限中文
$MYSQL < patch-utf8-fix-garbled.sql
```

权威全量 DDL 见 `backend/sql/schema.sql`（不含 seed；seed 以 `mysql-init.sql` + 各 `patch-*.sql` 为准）。
