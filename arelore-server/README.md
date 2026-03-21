# Arelore Server

后端服务项目，提供 RESTful API 服务。

## 技术栈

- **Java**: 21
- **Maven**: 3.8
- **Spring Boot**: 3.2.x (推荐最新版本)
- **数据库**: MySQL / PostgreSQL (可选)
- **缓存**: Redis (可选)

## 项目结构

```
arelore-server/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com.arelore.server/
│   │   │       ├── AreloreServerApplication.java  # 启动类
│   │   │       ├── controller/                     # 控制器层
│   │   │       ├── service/                        # 服务层
│   │   │       ├── repository/                     # 数据访问层
│   │   │       ├── entity/                         # 实体类
│   │   │       ├── dto/                            # 数据传输对象
│   │   │       ├── config/                         # 配置类
│   │   │       └── util/                           # 工具类
│   │   └── resources/
│   │       ├── application.yml                     # 配置文件
│   │       └── application-dev.yml                 # 开发环境配置
│   └── test/
│       └── java/
│           └── com.arelore.server/
│               └── controller/                      # 测试类
├── pom.xml                                         # Maven 配置
└── README.md                                       # 说明文档
```

## 快速开始

### 环境要求
- JDK 21+
- Maven 3.8+

### 安装依赖
```bash
mvn clean install
```

### 运行应用
```bash
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动

### 构建生产包
```bash
mvn clean package -DskipTests
```

### 运行测试
```bash
mvn test
```

## API 接口规划

### 用户模块
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/logout` - 用户登出
- `GET /api/user/profile` - 获取用户信息
- `PUT /api/user/profile` - 更新用户信息

### 产品模块
- `GET /api/products` - 获取产品列表
- `GET /api/products/{id}` - 获取产品详情
- `POST /api/products` - 创建产品
- `PUT /api/products/{id}` - 更新产品
- `DELETE /api/products/{id}` - 删除产品

### 消息模块
- `GET /api/messages` - 获取消息列表
- `GET /api/messages/unread` - 获取未读消息数
- `PUT /api/messages/{id}/read` - 标记消息为已读

## 配置说明

### 开发环境配置 (application-dev.yml)
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/arelore?useSSL=false&serverTimezone=UTC
    username: root
    password: your_password
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

jwt:
  secret: your-secret-key
  expiration: 86400000
```

## 开发规范

1. **代码风格**: 遵循阿里巴巴 Java 开发手册
2. **RESTful**: 遵循 RESTful API 设计规范
3. **异常处理**: 统一异常处理和响应格式
4. **日志记录**: 使用 SLF4J + Logback
5. **文档**: 使用 Swagger/OpenAPI 生成 API 文档

## 下一步计划

- [ ] 初始化 Spring Boot 项目
- [ ] 配置数据库连接
- [ ] 实现用户认证（JWT）
- [ ] 创建基础 CRUD 接口
- [ ] 集成 Swagger 文档
- [ ] 编写单元测试
- [ ] 配置 Docker 部署

## 常用 Maven 命令

```bash
# 清理项目
mvn clean

# 安装依赖
mvn install

# 运行项目
mvn spring-boot:run

# 打包项目
mvn package

# 运行测试
mvn test

# 跳过测试打包
mvn package -DskipTests
```
