---
name: arelore-backend-conventions
description: >-
  Enforces Arelore Java backend conventions for MySQL DDL, Service interfaces,
  and HTTP controllers. Use when writing or reviewing SQL table DDL, MyBatis
  entities, Service/ServiceImpl, RestController, @RequestMapping, Spring MVC
  APIs, or any arelore-server code under arelore-server-user, arelore-server-admin,
  or arelore-server-core.
---

# Arelore 后端编码规范

编写或修改 `arelore-server` 代码时必须遵守以下三条。不得用 REST 路径参数、GET query、或非 `BaseService` 的 Service 接口绕过。

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
