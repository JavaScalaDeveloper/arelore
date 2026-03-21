# Arelore Server Common

通用工具模块，提供基础的工具类、常量、异常处理等。

## 模块结构

```
arelore-server-common/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com.arelore.server.common/
│   │           ├── result/          # 统一返回结果
│   │           │   ├── Result.java         # 通用返回结果封装
│   │           │   └── ResultCode.java     # 返回状态码枚举
│   │           ├── exception/       # 异常处理
│   │           │   └── BusinessException.java  # 业务异常
│   │           ├── util/            # 工具类
│   │           │   ├── CommonUtils.java      # 通用工具类
│   │           │   └── DateUtils.java        # 日期时间工具类
│   │           ├── constant/        # 常量类
│   │           │   └── CommonConstants.java  # 通用常量
│   │           └── dto/             # 数据传输对象
│   │               └── PageResult.java       # 分页结果封装
│   └── test/
│       └── java/
│           └── com.arelore.server.common/
└── pom.xml
```

## 功能说明

### 1. 统一返回结果 (result)

#### Result<T>
通用的 API 返回结果封装，支持泛型数据。

**使用示例**:
```java
// 成功返回
Result<User> result = Result.success(user);

// 失败返回
Result<Void> result = Result.error("操作失败");

// 使用枚举
Result<Void> result = Result.error(ResultCode.USER_NOT_LOGIN);

// Builder 模式
Result<User> result = Result.<User>builder()
    .code(200)
    .message("获取成功")
    .data(user)
    .build();
```

#### ResultCode
预定义的状态码枚举，包含：
- 成功状态码 (SUCCESS)
- 客户端错误 (BAD_REQUEST, UNAUTHORIZED, FORBIDDEN, NOT_FOUND)
- 服务端错误 (INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE)
- 业务错误 (BUSINESS_ERROR, DATA_NOT_FOUND, 等)
- 用户相关 (USER_NOT_LOGIN, USER_TOKEN_EXPIRED, 等)
- 系统相关 (SYSTEM_BUSY, SYSTEM_MAINTENANCE)
- 文件相关 (FILE_UPLOAD_FAILED, FILE_NOT_FOUND, 等)

### 2. 异常处理 (exception)

#### BusinessException
自定义业务异常，支持多种构造方式。

**使用示例**:
```java
// 使用默认错误码
throw new BusinessException("用户不存在");

// 指定错误码
throw new BusinessException(1001, "自定义错误信息");

// 使用枚举
throw new BusinessException(ResultCode.USER_NOT_LOGIN);

// 带原因的异常
throw new BusinessException("操作失败", cause);
```

### 3. 工具类 (util)

#### CommonUtils
通用工具类，提供：
- 空值判断 (isEmpty, isNotEmpty)
- JSON 序列化/反序列化 (toJson, fromJson)
- 集合创建 (of, ofList, ofMap, ofSet)
- 安全获取 (safeGet)
- 类型转换 (toInteger, toLong, toDouble)
- 字符串处理 (trim)

**使用示例**:
```java
// 空值判断
boolean empty = CommonUtils.isEmpty(str);
boolean notEmpty = CommonUtils.isNotEmpty(list);

// JSON 处理
String json = CommonUtils.toJson(obj);
User user = CommonUtils.fromJson(json, User.class);
List<User> users = CommonUtils.jsonToList(json, User.class);

// 创建集合
Map<String, Object> map = CommonUtils.ofMap("key1", value1, "key2", value2);
List<String> list = CommonUtils.ofList("a", "b", "c");
Set<Integer> set = CommonUtils.ofSet(1, 2, 3);

// 安全获取
String value = CommonUtils.safeGet(map, key, "default");
Integer item = CommonUtils.safeGet(list, index, defaultValue);

// 类型转换
Integer num = CommonUtils.toInteger("123");
Long num = CommonUtils.toLong("456");
Double num = CommonUtils.toDouble("789.01");
```

#### DateUtils
日期时间工具类，提供：
- 当前时间获取 (getCurrentDate, getCurrentTime, getCurrentDateTime)
- 格式化 (formatDate, formatTime, formatDateTime)
- 解析 (parseDate, parseTime, parseDateTime)
- 时间戳转换 (timestampToDateTime, dateTimeToTimestamp)
- 日期计算 (daysBetween, hoursBetween)
- 特殊日期判断 (isToday, isYesterday)
- 边界日期获取 (getFirstDayOfMonth, getLastDayOfYear)
- Date 与 LocalDateTime 互转

**使用示例**:
```java
// 获取当前时间
String now = DateUtils.getCurrentDateTime();

// 格式化
String dateStr = DateUtils.formatDate(LocalDate.now(), "yyyy-MM-dd");

// 解析
LocalDate date = DateUtils.parseDate("2024-01-01", "yyyy-MM-dd");

// 时间戳
Long timestamp = DateUtils.getCurrentTimestamp();
LocalDateTime dateTime = DateUtils.timestampToDateTime(timestamp);

// 日期计算
Long days = DateUtils.daysBetween(startDate, endDate);

// 判断
boolean today = DateUtils.isToday(dateTime);

// 边界日期
LocalDate firstDay = DateUtils.getFirstDayOfMonth(LocalDate.now());
LocalDate lastDay = DateUtils.getLastDayOfMonth(LocalDate.now());
```

### 4. 常量类 (constant)

#### CommonConstants
通用常量定义，包含：
- 状态码常量 (SUCCESS_CODE, ERROR_CODE, 等)
- 布尔常量 (YES, NO)
- 分页常量 (DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE, 等)
- Token 相关 (TOKEN_PREFIX, TOKEN_HEADER)
- 编码常量 (UTF8, GBK)
- 分隔符常量 (COMMA, SEMICOLON, COLON, 等)
- 角色常量 (ROLE_ADMIN, ROLE_USER, SUPER_ADMIN)
- 状态常量 (STATUS_ENABLE, STATUS_DISABLE)
- 删除标志 (DELETED_NO, DELETED_YES)
- 正则表达式 (EMAIL_REGEX, MOBILE_REGEX, 等)

**使用示例**:
```java
// 状态码
if (code.equals(CommonConstants.SUCCESS_CODE)) { ... }

// 分页
Integer pageNum = CommonConstants.DEFAULT_PAGE_NUM;
Integer pageSize = CommonConstants.DEFAULT_PAGE_SIZE;

// Token
String token = request.getHeader(CommonConstants.TOKEN_HEADER);

// 正则匹配
if (email.matches(CommonConstants.EMAIL_REGEX)) { ... }
```

### 5. 数据传输对象 (dto)

#### PageResult<T>
分页结果封装，包含：
- 分页参数 (pageNum, pageSize)
- 总数信息 (total, totalPages)
- 数据列表 (list)
- 导航信息 (hasPrevious, hasNext)

**使用示例**:
```java
// 构建分页结果
PageResult<User> pageResult = PageResult.of(
    userList,
    1,          // 当前页
    10,         // 每页大小
    100L        // 总记录数
);

// 空的分页结果
PageResult<User> empty = PageResult.empty(1, 10);
```

## 依赖说明

本模块依赖以下第三方库：
- **Spring Boot**: 基础框架支持
- **Lombok**: 简化代码编写
- **Hutool**: Java 工具类库
- **Guava**: Google 核心库
- **FastJSON2**: JSON 处理
- **Apache Commons Lang3**: Apache 通用工具

## 使用方式

在其他模块的 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>com.arelore</groupId>
    <artifactId>arelore-server-common</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 最佳实践

1. **统一返回**: 所有 Controller 方法都使用 `Result<T>` 包装返回值
2. **异常处理**: 业务逻辑中使用 `BusinessException` 抛出异常
3. **工具类优先**: 优先使用已有的工具类，避免重复造轮子
4. **常量集中管理**: 公共常量定义在 `CommonConstants` 中
5. **分页标准化**: 所有分页接口统一使用 `PageResult<T>` 返回

## 注意事项

1. 工具类都是线程安全的，可以直接调用静态方法
2. 所有类都实现了 `Serializable` 接口，支持序列化
3. 使用了 Lombok 注解，需要 IDE 安装相应插件
4. 日期时间默认使用系统时区

## 扩展建议

可以根据业务需要添加更多工具类：
- `FileUtils`: 文件处理工具
- `ImageUtils`: 图片处理工具
- `EncryptUtils`: 加密解密工具
- `BeanUtils`: Bean 拷贝工具
- `ValidationUtils`: 参数校验工具
