---
name: arelore-backend-conventions
description: >-
  Enforces Arelore Java backend conventions for MySQL DDL, Service interfaces,
  HTTP controllers, and Response VO idStr for long numeric IDs. Use when writing
  or reviewing SQL table DDL, MyBatis entities, Service/ServiceImpl, RestController,
  XxxResponse/XxxRequest DTOs, @RequestMapping, Spring MVC APIs, or any
  arelore-server code under arelore-server-user, arelore-server-admin, or
  arelore-server-core.
---

# Arelore 后端编码规范

编写或修改 `arelore-server` 代码时必须遵守以下四条。不得用 REST 路径参数、GET query、或非 `BaseService` 的 Service 接口绕过。

## 1. MySQL 建表 DDL

表定义的**前 3 个字段**必须是：

```sql
`id`                    bigint auto_increment comment '主键ID',
    `create_time`           datetime              default current_timestamp not null comment '创建时间',
    `modify_time`           datetime              default current_timestamp not null on update current_timestamp comment '修改时间',
```

且索引**至少包含**这 3 个：

```sql
primary key (`id`),
    key `idx_create_time` (`create_time`),
    key `idx_modify_time` (`modify_time`)
```

业务字段写在 `modify_time` 之后；业务索引写在上述三个索引之后。禁止把 `id` / `create_time` / `modify_time` 放到中间或末尾，禁止省略 `idx_create_time`、`idx_modify_time`。

## 2. Service 接口

所有的 Service 接口都继承 `com.arelore.server.core.service.BaseService`。

`XxxRequest`、`XxxResponse` 都是 `Xxx`（db 表实体类）的子类，如果没有就创建它。约定：`XxxResponse extends Xxx`，`XxxRequest extends XxxResponse`。

```java
public interface FooService extends BaseService<FooRequest, FooResponse> {
}

@Data
@EqualsAndHashCode(callSuper = true)
public class FooResponse extends Foo {
}

@Data
@EqualsAndHashCode(callSuper = true)
public class FooRequest extends FooResponse {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
```

实现类走现有的 `BaseServiceImpl`。不要新建不继承 `BaseService` 的 `*Service` 接口。写 Service 前先确认实体、`XxxRequest`、`XxxResponse` 存在，缺哪个补哪个。

## 3. Controller

所有 Controller 层的代码使用 POST 请求 + `@RequestBody`，严禁将参数写在路径上。

- 用 `@PostMapping`，不要用 `@GetMapping` / `@PutMapping` / `@DeleteMapping` / `@PatchMapping` 暴露业务接口。
- 入参用 `@RequestBody` DTO，不要用 `@PathVariable`、`@RequestParam`、路径占位 `{id}`。
- 路径只表达操作名，例如 `/api/user/auth/wechat/quick`，不要 `/api/user/{userId}`。

```java
@PostMapping("/logout")
public Result<Void> logout(@RequestBody LogoutRequest request) { ... }
```

错误示例：`@GetMapping("/{id}")`、`@PostMapping("/get/{id}")`、`public Result get(@PathVariable Long id)`。

## 4. Response VO 必须带 idStr（防前端精度溢出）

实体里的数值 ID 字段（如 `Long id`、`BigDecimal userId`）**保持类型不变**，不要为了前端改成 String。

凡会返回给前端、且可能超过 JavaScript 安全整数（`Number.MAX_SAFE_INTEGER`，约 16 位）的业务 ID，必须在对应的 **`XxxResponse`（VO）** 上增加 **`idStr`（String）**：

- `idStr` = 该业务 ID 的十进制字符串（`BigDecimal` 用 `toPlainString()`，`Long` 用 `String.valueOf`）。
- 典型场景：20 位 `user_id`（`DECIMAL(20,0)` / `BigDecimal`）。前端列表展示、二次查询（学习记录等）**只用 `idStr`**，禁止依赖 JSON 数字形态的 `userId` / 超长 `id`。
- 在 `ServiceImpl.toResponse`（或等价组装处）填充 `idStr`；列表/分页/详情凡走 `toResponse`/`toResponses` 的路径都要带上。
- 查询入参若接收前端传来的业务 ID，优先支持 `idStr`，在 `buildWrapper` 内解析为实体上的数值字段再查库（可参考 `WordUserIdStrSupport`）。

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class FooResponse extends Foo {
    /** 业务 ID 字符串，对应易溢出的数值 ID；前端展示与回传用本字段 */
    private String idStr;
}
```

主键 `id` 为普通自增 `bigint` 且远小于安全整数时，可不额外加 `idStr`；一旦存在超长业务 ID（尤其 `userId`），对应 VO **必须**提供 `idStr`。
