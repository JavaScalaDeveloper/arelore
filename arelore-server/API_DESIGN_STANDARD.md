# Arelore Server API 设计规范

## ⚠️ 重要规定

**本服务端一律禁止使用 RESTful 风格！**

所有接口必须遵循以下规范：
1. ✅ **路径上不允许有参数**（禁止使用 `@PathVariable`）
2. ✅ **所有入参都必须为 Body**（统一使用 `@RequestBody`）
3. ✅ **统一使用 POST 方法**（不使用 GET、PUT、DELETE 等）
4. ✅ **路径命名规范**：`/api/{模块}/{操作名}`

## 规范说明

### ❌ 错误的写法（RESTful 风格）

```java
// 错误 1: 路径上有参数
@GetMapping("/user/{id}")
public Result<User> getUser(@PathVariable String id) { }

// 错误 2: 使用 PUT 方法
@PutMapping("/user/{id}")
public Result<Void> updateUser(@PathVariable String id, @RequestBody User user) { }

// 错误 3: 使用 DELETE 方法
@DeleteMapping("/user/{id}")
public Result<Void> deleteUser(@PathVariable String id) { }

// 错误 4: 查询参数在 URL 上
@GetMapping("/user/list?pageNum=1&pageSize=10")
public Result<List<User>> getList(@RequestParam Integer pageNum, @RequestParam Integer pageSize) { }
```

### ✅ 正确的写法（非 RESTful 风格）

```java
// 正确 1: 所有参数都在 Body 中
@PostMapping("/user/detail")
public Result<User> getUser(@RequestBody UserQueryRequest request) { }

// 正确 2: 统一使用 POST
@PostMapping("/user/update")
public Result<Void> updateUser(@RequestBody UserSaveRequest request) { }

// 正确 3: 删除操作也使用 POST
@PostMapping("/user/delete")
public Result<Void> deleteUser(@RequestBody UserDeleteRequest request) { }

// 正确 4: 查询参数也在 Body 中
@PostMapping("/user/list")
public Result<List<User>> getList(@RequestBody UserQueryRequest request) { }
```

## DTO 设计规范

每个操作都应该有对应的 Request DTO 类：

### 示例：用户查询请求

```java
@Data
public class UserQueryRequest {
    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 用户名（可选）
     */
    private String username;
}
```

### 示例：用户保存请求

```java
@Data
public class UserSaveRequest {
    /**
     * 用户 ID（更新时必填）
     */
    private String id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;
}
```

## 接口命名规范

### 用户服务 (/api/user)

| 操作 | 路径 | 请求 DTO | 说明 |
|------|------|----------|------|
| 查询列表 | `POST /api/user/list` | UserQueryRequest | 分页查询用户列表 |
| 查看详情 | `POST /api/user/detail` | UserDeleteRequest | 获取单个用户详情 |
| 创建用户 | `POST /api/user/create` | UserSaveRequest | 创建新用户 |
| 更新用户 | `POST /api/user/update` | UserSaveRequest | 更新用户信息 |
| 删除用户 | `POST /api/user/delete` | UserDeleteRequest | 删除用户 |

### 管理员服务 (/api/admin)

| 操作 | 路径 | 请求 DTO | 说明 |
|------|------|----------|------|
| 登录 | `POST /api/admin/login` | AdminLoginRequest | 管理员登录 |
| 仪表盘 | `POST /api/admin/dashboard` | - | 获取统计数据 |
| 用户列表 | `POST /api/admin/users` | AdminUserQueryRequest | 查询用户列表 |
| 获取设置 | `POST /api/admin/settings/get` | - | 获取系统设置 |
| 更新设置 | `POST /api/admin/settings/update` | SystemSettingsRequest | 更新系统配置 |

## 为什么不用 RESTful？

我们选择不用 RESTful 风格的理由：

1. **统一性**: 所有接口都使用 POST，前端调用方式一致
2. **灵活性**: Body 可以传递复杂对象，不受 URL 长度限制
3. **安全性**: 敏感参数不在 URL 中显示
4. **可维护性**: 接口变更不影响 URL 结构
5. **简化设计**: 不需要考虑 HTTP 方法的语义

## 代码审查要点

在代码审查时，请检查以下内容：

- [ ] 是否使用了 `@PathVariable`（禁止）
- [ ] 是否使用了 `@RequestParam`（禁止）
- [ ] 是否只使用了 `@PostMapping`（只能使用 POST）
- [ ] 是否有对应的 Request DTO 类
- [ ] DTO 字段是否有完整的注释
- [ ] 路径命名是否符合规范

## 配置文件说明

每个服务的 `application.yml` 中都添加了设计规范注释，提醒开发者遵守规范。

示例：
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

## 工具支持

虽然 YAML 配置无法强制限制 API 风格，但可以通过以下方式保证规范执行：

1. **代码审查**: 严格执行上述审查要点
2. **IDE 模板**: 创建 Controller 和 DTO 的代码模板
3. **静态检查**: 可以编写 CheckStyle 或 Sonar 规则检测违规用法
4. **单元测试**: 测试用例覆盖所有接口，确保参数传递正确

## 总结

**核心原则**: 
- 🚫 不用 RESTful
- 🚫 路径无参数
- ✅ 全用 POST
- ✅ 参数在 Body

请所有开发人员严格遵守此规范！
