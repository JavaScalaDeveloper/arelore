# Arelore Server Modules

Arelore 后端服务模块，采用微服务架构设计。

## 模块结构

```
arelore-server/
├── pom.xml                          # 父 POM，管理版本和依赖
├── arelore-server-common/           # 通用工具模块 ⭐
│   └── 提供 Result、PageResult、工具类等
├── arelore-server-user/             # 用户服务模块 👤
│   └── 提供用户相关的 API 接口
└── arelore-server-admin/            # 管理员服务模块 🔐
    └── 提供管理员专用的 API 接口
```

## 模块说明

### 1. arelore-server-common（通用模块）⭐

**定位**: 被其他所有模块依赖的基础模块

**功能**:
- ✅ `Result<T>` - 统一返回结果封装
- ✅ `ResultCode` - 状态码枚举
- ✅ `PageResult<T>` - 分页结果封装
- ✅ `BusinessException` - 业务异常类
- ✅ `CommonUtils` - 通用工具类
- ✅ `DateUtils` - 日期时间工具类
- ✅ `CommonConstants` - 通用常量类

**使用方式**:
```xml
<dependency>
    <groupId>com.arelore</groupId>
    <artifactId>arelore-server-common</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. arelore-server-user（用户服务）👤

**端口**: 8081

**定位**: 为普通用户提供服务的模块

**API 接口**:
- `GET /api/user/list` - 获取用户列表（分页）
- `GET /api/user/{id}` - 获取用户详情
- `POST /api/user` - 创建用户
- `PUT /api/user/{id}` - 更新用户
- `DELETE /api/user/{id}` - 删除用户

**启动方式**:
```bash
cd arelore-server/arelore-server-user
mvn spring-boot:run
```

**访问地址**: http://localhost:8081

### 3. arelore-server-admin（管理员服务）🔐

**端口**: 8082

**定位**: 为管理员提供专用服务的模块

**API 接口**:
- `POST /api/admin/login` - 管理员登录
- `GET /api/admin/dashboard` - 获取仪表盘数据
- `GET /api/admin/users` - 获取用户列表（管理员视角）
- `GET /api/admin/settings` - 获取系统设置
- `PUT /api/admin/settings` - 更新系统设置

**启动方式**:
```bash
cd arelore-server/arelore-server-admin
mvn spring-boot:run
```

**访问地址**: http://localhost:8082

## 快速开始

### 1. 安装依赖

```bash
cd arelore-server
mvn clean install
```

### 2. 启动服务

**启动用户服务**:
```bash
cd arelore-server/arelore-server-user
mvn spring-boot:run
```

**启动管理员服务**:
```bash
cd arelore-server/arelore-server-admin
mvn spring-boot:run
```

### 3. 测试接口

**测试用户服务**:
```bash
curl http://localhost:8081/api/user/list
```

**测试管理员服务**:
```bash
curl http://localhost:8082/api/admin/dashboard
```

## 架构设计理念

### 为什么按用户类型拆分？

1. **职责分离**: 用户服务和管理员服务关注点不同
2. **安全隔离**: 管理员接口需要更高级别的安全控制
3. **独立扩展**: 可以根据负载独立扩展某个服务
4. **便于维护**: 代码组织清晰，易于理解和维护

### 为什么不包含 "web"？

- ✅ **多端支持**: 服务端代码服务于 Web、小程序、Android、iOS 等多种客户端
- ✅ **语义清晰**: "user" 和 "admin" 明确表达服务对象
- ✅ **避免歧义**: "web" 容易与前端混淆

### 为什么不用 RESTful 风格？

本项目**一律禁止使用 RESTful 风格**，所有接口统一遵循：
- 🚫 路径上不允许有参数（不使用 `@PathVariable`）
- 🚫 查询参数不在 URL 上（不使用 `@RequestParam`）
- ✅ 所有入参都必须为 Body（统一使用 `@RequestBody`）
- ✅ 统一使用 POST 方法（不使用 GET、PUT、DELETE）

详细规范请参考 [`API_DESIGN_STANDARD.md`](API_DESIGN_STANDARD.md)

## 服务调用关系

```
┌─────────────────┐
│  客户端应用     │
│  (Web/小程序等) │
└────────┬────────┘
         │
         ├──────────────────────┐
         │                      │
         ▼                      ▼
┌─────────────────┐   ┌─────────────────┐
│  User Service   │   │ Admin Service   │
│   (端口 8081)   │   │   (端口 8082)   │
└─────────────────┘   └─────────────────┘
         │                      │
         └──────────┬───────────┘
                    │
                    ▼
         ┌─────────────────────┐
         │  Common Module      │
         │  (被所有服务依赖)   │
         └─────────────────────┘
```

## 开发规范

### 1. 依赖管理

- 所有子模块必须继承根 POM
- 通用依赖在根 POM 的 `<dependencyManagement>` 中管理
- 子模块只声明需要的依赖，不重复声明版本

### 2. 包命名规范

- 基础包名：`com.arelore.server.{module}`
- 示例：
  - `com.arelore.server.user.controller`
  - `com.arelore.server.admin.service`

### 3. 接口设计规范

- 所有接口必须返回 `Result<T>` 对象
- 分页接口使用 `PageResult<T>` 封装
- 异常统一使用 `BusinessException`

### 4. 配置文件规范

每个服务独立的 `application.yml`:
```yaml
server:
  port: 808x  # 每个服务使用不同的端口

spring:
  application:
    name: arelore-server-{service-name}
```

## 下一步计划

- [ ] 集成数据库（MySQL/PostgreSQL）
- [ ] 实现 JWT 认证
- [ ] 添加 Redis 缓存
- [ ] 实现完整的 CRUD 操作
- [ ] 添加 Swagger 文档
- [ ] 编写单元测试
- [ ] 集成消息队列（可选）
- [ ] 添加日志记录
- [ ] 实现服务间通信

## 常见问题

**Q: 为什么要拆分成多个服务？**
A: 便于独立开发、部署和扩展，符合微服务架构理念。

**Q: 可以合并成一个服务吗？**
A: 初期可以合并，但随着业务发展，拆分更有利于维护。

**Q: 如何管理服务间的依赖？**
A: 通过根 POM 统一管理版本，common 模块存放共享代码。

**Q: 端口冲突怎么办？**
A: 修改对应服务的 `application.yml` 中的端口配置。

## 总结

当前架构特点：
- ✅ **清晰的模块划分**: 按服务对象分为 user 和 admin
- ✅ **统一的工具支持**: common 模块提供通用能力
- ✅ **独立的部署单元**: 每个服务可独立运行
- ✅ **易于扩展**: 可随时添加新的服务模块
