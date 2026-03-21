# API 设计规范变更说明

## 变更概述

**变更日期**: 2026-03-22  
**变更内容**: 服务端 API 从 RESTful 风格改为全 POST + Body 参数风格

## 变更原因

根据项目要求，服务端一律禁止使用 RESTful 风格，所有接口必须遵循：
1. 路径上不允许有参数
2. 所有入参都必须为 Body

## 已完成的修改

### 1. 用户服务模块 (arelore-server-user)

#### 修改前（RESTful 风格）❌

```java
// GET 请求，查询参数在 URL
@GetMapping("/list")
public Result<PageResult<String>> getUserList(
    @RequestParam Integer pageNum,
    @RequestParam Integer pageSize) { }

// GET 请求，路径参数
@GetMapping("/{id}")
public Result<String> getUserDetail(@PathVariable String id) { }

// PUT 请求，路径参数 + Body
@PutMapping("/{id}")
public Result<String> updateUser(
    @PathVariable String id, 
    @RequestBody UserSaveRequest request) { }

// DELETE 请求，路径参数
@DeleteMapping("/{id}")
public Result<String> deleteUser(@PathVariable String id) { }
```

#### 修改后（非 RESTful 风格）✅

```java
// POST 请求，所有参数在 Body
@PostMapping("/list")
public Result<PageResult<String>> getUserList(@RequestBody UserQueryRequest request) { }

// POST 请求，参数在 Body
@PostMapping("/detail")
public Result<String> getUserDetail(@RequestBody UserDeleteRequest request) { }

// POST 请求，参数在 Body
@PostMapping("/update")
public Result<String> updateUser(@RequestBody UserSaveRequest request) { }

// POST 请求，参数在 Body
@PostMapping("/delete")
public Result<String> deleteUser(@RequestBody UserDeleteRequest request) { }
```

#### 新增 DTO 类

- ✅ `UserQueryRequest.java` - 用户查询请求 DTO
- ✅ `UserSaveRequest.java` - 用户创建/更新请求 DTO
- ✅ `UserDeleteRequest.java` - 用户删除请求 DTO

### 2. 管理员服务模块 (arelore-server-admin)

#### 修改前（混合风格）❌

```java
// POST 请求（正确）
@PostMapping("/login")
public Result<Map> login(@RequestBody Map<String, String> loginDto) { }

// GET 请求，无参数（错误）
@GetMapping("/dashboard")
public Result<Map> getDashboard() { }

// GET 请求，查询参数在 URL（错误）
@GetMapping("/users")
public Result<PageResult> getUserList(
    @RequestParam Integer pageNum,
    @RequestParam Integer pageSize) { }

// PUT 请求（错误）
@PutMapping("/settings")
public Result<String> updateSettings(@RequestBody Map settings) { }
```

#### 修改后（统一 POST + Body）✅

```java
// POST 请求，参数在 Body
@PostMapping("/login")
public Result<Map> login(@RequestBody AdminLoginRequest request) { }

// POST 请求，无参数（允许）
@PostMapping("/dashboard")
public Result<Map> getDashboard() { }

// POST 请求，参数在 Body
@PostMapping("/users")
public Result<PageResult> getUserList(@RequestBody AdminUserQueryRequest request) { }

// POST 请求，参数在 Body
@PostMapping("/settings/update")
public Result<String> updateSettings(@RequestBody SystemSettingsRequest request) { }
```

#### 新增 DTO 类

- ✅ `AdminLoginRequest.java` - 管理员登录请求 DTO
- ✅ `AdminUserQueryRequest.java` - 管理员查询用户请求 DTO
- ✅ `SystemSettingsRequest.java` - 系统设置请求 DTO

### 3. 配置文件添加规范说明

#### application.yml (User Service)

```yaml
# =====================================================
# Arelore Server User Service Configuration
# =====================================================
# API 设计规范：
# 1. 禁止使用 RESTful 风格（路径上不允许有参数）
# 2. 所有入参必须通过 @RequestBody 传递（Body 参数）
# 3. 统一使用 POST 方法
# 4. 路径命名规范：/api/user/{操作名}，如 /api/user/list
# =====================================================
```

#### application.yml (Admin Service)

```yaml
# =====================================================
# Arelore Server Admin Service Configuration
# =====================================================
# API 设计规范：
# 1. 禁止使用 RESTful 风格（路径上不允许有参数）
# 2. 所有入参必须通过 @RequestBody 传递（Body 参数）
# 3. 统一使用 POST 方法
# 4. 路径命名规范：/api/admin/{操作名}，如 /api/admin/login
# =====================================================
```

### 4. 新增文档

- ✅ `API_DESIGN_STANDARD.md` - API 设计规范详细文档
- ✅ `CHANGELOG_API_STYLE.md` - 本变更说明文档（当前文件）

## 接口对比表

### 用户服务接口

| 功能 | 旧方式（RESTful） | 新方式（非 RESTful） |
|------|-----------------|-------------------|
| 查询列表 | `GET /api/user/list?page=1&size=10` | `POST /api/user/list` (Body: `{pageNum:1,pageSize:10}`) |
| 查看详情 | `GET /api/user/{id}` | `POST /api/user/detail` (Body: `{id:"xxx"}`) |
| 创建用户 | `POST /api/user` (Body) | `POST /api/user/create` (Body) |
| 更新用户 | `PUT /api/user/{id}` (Body) | `POST /api/user/update` (Body: `{id:"xxx",...}`) |
| 删除用户 | `DELETE /api/user/{id}` | `POST /api/user/delete` (Body: `{id:"xxx"}`) |

### 管理员服务接口

| 功能 | 旧方式（混合） | 新方式（非 RESTful） |
|------|---------------|-------------------|
| 登录 | `POST /api/admin/login` (Body) | `POST /api/admin/login` (Body) |
| 仪表盘 | `GET /api/admin/dashboard` | `POST /api/admin/dashboard` |
| 用户列表 | `GET /api/admin/users?page=1&size=10` | `POST /api/admin/users` (Body) |
| 获取设置 | `GET /api/admin/settings` | `POST /api/admin/settings/get` |
| 更新设置 | `PUT /api/admin/settings` (Body) | `POST /api/admin/settings/update` (Body) |

## 技术影响

### 优点 ✅

1. **统一性**: 所有接口调用方式一致，降低前端学习成本
2. **灵活性**: Body 可以传递复杂对象和嵌套结构
3. **安全性**: 敏感参数不在 URL 中暴露
4. **可扩展**: 添加参数不需要修改 URL 结构
5. **简化设计**: 不需要考虑 HTTP 方法的语义

### 缺点 ⚠️

1. **不符合 REST 规范**: 违背了 RESTful 的设计原则
2. **缓存支持差**: POST 请求默认不被缓存
3. **幂等性**: 需要额外机制保证操作的幂等性
4. **调试不便**: 无法直接在浏览器地址栏访问

## 迁移指南

### 前端调用示例

#### 修改前（RESTful）

```javascript
// GET 请求
axios.get('/api/user/list', {
  params: { pageNum: 1, pageSize: 10 }
});

// GET 请求带路径参数
axios.get(`/api/user/${userId}`);

// PUT 请求
axios.put(`/api/user/${userId}`, userData);

// DELETE 请求
axios.delete(`/api/user/${userId}`);
```

#### 修改后（非 RESTful）

```javascript
// POST 请求，参数在 Body
axios.post('/api/user/list', {
  pageNum: 1,
  pageSize: 10
});

// POST 请求，参数在 Body
axios.post('/api/user/detail', {
  id: userId
});

// POST 请求，参数在 Body
axios.post('/api/user/update', {
  id: userId,
  ...userData
});

// POST 请求，参数在 Body
axios.post('/api/user/delete', {
  id: userId
});
```

## 检查清单

确保以下项目已完成：

- [x] 移除所有 `@PathVariable` 注解
- [x] 移除所有 `@RequestParam` 注解
- [x] 将所有 `@GetMapping` 改为 `@PostMapping`
- [x] 将所有 `@PutMapping` 改为 `@PostMapping`
- [x] 将所有 `@DeleteMapping` 改为 `@PostMapping`
- [x] 为每个接口创建对应的 Request DTO
- [x] 在配置文件中添加规范说明
- [x] 编写 API 设计规范文档
- [x] 更新模块说明文档

## 后续工作

1. **代码审查**: 严格执行 API 设计规范
2. **前端适配**: 通知前端开发人员接口风格变更
3. **文档更新**: 持续完善 API 接口文档
4. **工具支持**: 考虑引入静态代码检查工具

## 相关文档

- [API 设计规范](API_DESIGN_STANDARD.md) - 详细的 API 设计规范
- [模块说明](MODULES.md) - 服务端模块架构说明

## 总结

本次变更是为了统一项目 API 设计风格，虽然牺牲了 RESTful 的某些特性，但换来了更高的统一性和灵活性。请所有开发人员严格遵守新的 API 设计规范！
