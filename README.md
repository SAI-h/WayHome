# WayHome · 公交线路规划系统

WayHome 是一个面向城市公共交通的**线路规划与信息管理系统**，包含：

- **用户端（userPlatform）**：面向乘客，提供城市选择、公交路径规划（最短耗时 / 最少换乘）、班次查询、站点线路查询等功能。
- **管理端（adminPlatform）**：面向管理员，提供站点、线路、班次的增删改查，并支持基于高德地图的可视化编辑。
- **后端（wayHome_backend）**：基于 Spring Boot 的 RESTful 服务，采用 A* / BFS 算法完成路径规划，并通过 RabbitMQ + Redis 实现异步任务与结果缓存。

---

## 一、技术栈

| 层次 | 技术 |
| --- | --- |
| 后端框架 | Spring Boot 3.4.2、MyBatis-Plus 3.5.5 |
| 数据存储 | MySQL、Redis、RabbitMQ |
| 认证 | JWT（java-jwt） |
| 数据库连接池 | Druid |
| 前端框架 | Vue 2.6 + Vue Router + Element UI |
| 地图 | 高德地图 JS API 2.0 |

---

## 二、目录结构

```
WayHome
├── wayHome_backend                  # 后端服务
│   ├── pom.xml
│   └── src/main
│       ├── java/com/example/wayhome
│       │   ├── config               # 配置类（JWT / Redis / RabbitMQ / Web）
│       │   ├── constant             # 常量（Redis 键 / RabbitMQ 路由键）
│       │   ├── consumer             # RabbitMQ 消费者
│       │   ├── controller           # 控制层
│       │   ├── convert              # DTO / 实体 / VO 转换器
│       │   ├── dto                  # 数据传输对象
│       │   ├── entity               # 数据库实体
│       │   ├── exception            # 异常处理
│       │   ├── graph                # 图算法实体（Edge / Graph / Node）
│       │   ├── interceptor          # JWT 拦截器
│       │   ├── mapper               # MyBatis Mapper
│       │   ├── serialization        # Redis 序列化辅助
│       │   ├── service              # 业务接口与实现
│       │   ├── utils                # 工具类（Result / MD5）
│       │   └── vo                   # 视图对象
│       └── resources/application.yaml
│
└── wayHome_frontend                 # 前端
    ├── adminPlatform                # 管理端（端口 18080）
    │   └── src
    │       ├── components           # 登录、首页、站点/线路/班次管理
    │       ├── config               # 全局配置
    │       ├── router               # 路由
    │       └── utils                # Axios 请求封装
    └── userPlatform                 # 用户端（端口 18081）
        └── src
            ├── components           # 登录、首页、路径规划、班次查询
            ├── MapContainer         # 地图容器
            ├── config               # 全局配置
            ├── router               # 路由
            └── utils                # Axios 请求封装
```

---

## 三、环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 5+
- RabbitMQ 3.9+
- Node.js 16+（建议 18+）

---

## 四、快速启动

### 1. 准备中间件

确保 MySQL、Redis、RabbitMQ 已启动，并按需修改后端配置：

```yaml
# wayHome_backend/src/main/resources/application.yaml
spring:
  datasource:
    druid:
      url: jdbc:mysql://localhost:3306/bus_dataset?useUnicode=true&characterEncoding=utf8
      username: root
      password: ${DB_PASSWORD}
  data:
    redis:
      port: 6379
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

> 数据库 `bus_dataset` 需包含系统对应的表结构（admin、city、point、route、route_point、schedule、station）。

### 2. 启动后端

```bash
cd wayHome_backend
mvn spring-boot:run
```

后端默认运行在 `http://localhost:3000`。

### 3. 启动管理端

```bash
cd wayHome_frontend/adminPlatform
npm install
npm run serve
```

管理端默认运行在 `http://localhost:18080`。

### 4. 启动用户端

```bash
cd wayHome_frontend/userPlatform
npm install
npm run serve
```

用户端默认运行在 `http://localhost:18081`。

> 后端地址可通过环境变量 `VUE_APP_BASE_URL` 覆盖，默认 `http://localhost:3000`（见各平台 `src/config/index.js`）。

---

## 五、核心接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/admin` | 管理员登录，返回 JWT |
| GET | `/city` | 获取城市列表 |
| POST | `/station` | 新增站点 |
| GET | `/station` | 查询站点 |
| PATCH | `/station` | 更新站点 |
| DELETE | `/station` | 删除站点 |
| POST | `/route` | 新增线路 |
| GET | `/route` | 查询线路 |
| PATCH | `/route` | 更新线路 |
| DELETE | `/route/{routeID}` | 删除线路 |
| POST | `/schedule` | 新增班次 |
| GET | `/schedule/{routeID}` | 按线路查询班次 |
| GET | `/calPath` | 提交路径规划任务 |
| GET | `/calPath/result` | 轮询查询规划结果 |

---

## 六、路径规划设计

1. 用户端调用 `/calPath` 提交起点、终点、出发时间与方案类型（`minTimeCost` 最短耗时 / `minTransfer` 最少换乘）。
2. 后端生成 `taskID`，将任务投递到 RabbitMQ 交换机，并立即返回任务 ID。
3. 消费者消费任务：
   - 最短耗时：基于站点构建带权图，使用 **A\* 算法**求解，启发函数采用地理直线距离除以平均车速。
   - 最少换乘：使用 **BFS** 求解最少换乘次数方案。
4. 计算结果写入 Redis（键前缀 `path_cal_result:`），用户端通过 `/calPath/result` 轮询获取。

---

## 七、本次重构说明

### 后端（遵循《阿里巴巴 Java 开发手册》）

- 包名规范：`Interceptor`、`MedianValueSerialization`、`graphEntity` 调整为全小写 `interceptor`、`serialization`、`graph`。
- 枚举常量统一为 `UPPER_SNAKE_CASE`（如 `SIGNATURE_EXCEPTION`）。
- 方法命名规范为 `lowerCamelCase`（如 `convertToVo` / `convertToDo`）。
- 消除魔法值：新增 `constant/RedisKeyConstant`、`constant/RabbitMqConstant` 统一维护缓存键与路由键。
- 统一异常处理：`GlobalExceptionHandler` 新增参数校验异常处理，并对未知异常脱敏，避免堆栈信息外泄。
- 工具类改进：`MD5Util` 使用 `StandardCharsets.UTF_8`，移除 `printStackTrace`；工具类构造器私有化。
- 移除无用依赖与死代码（如未使用的 `PathCalDTO`、`fastjson`），补充缺失的 `spring-boot-starter-amqp`。
- 统一返回结果 `Result` 增加 `ok` / `fail` 便捷方法。
- 控制器改为构造器注入，补齐 Javadoc 注释。
- 修复 A* 启发函数中 `uniqueTagToPointID` 键偏移一位导致的启发函数失效问题。

### 前端（响应式 + 美化）

- 新增 `src/utils/request.js` 统一 Axios 请求封装（自动携带 JWT、统一错误提示）。
- 新增 `src/config/index.js` 集中管理后端地址，移除各组件硬编码。
- 重写管理端 / 用户端登录页：现代卡片式设计、渐变背景、响应式布局。
- 重写管理端首页：弹性布局 + 视口高度适配，适配不同屏幕尺寸。
- 全局样式规范化（`box-sizing`、字体栈、移动端媒体查询）。

---

## 八、说明与注意事项

- 前端地图使用了高德地图 JS API，`key` 与 `securityJsCode` 需替换为你自己的密钥。
- JWT 签名密钥 `jwt.secret` 建议通过环境变量注入，避免明文提交。
- 生产环境请关闭 `productionSourceMap`，并通过 `VUE_APP_BASE_URL` 指定后端地址。
