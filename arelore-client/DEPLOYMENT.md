# Arelore 前端应用部署说明

## 📋 端口配置

两个前端应用使用不同的端口启动，互不冲突：

| 应用 | 端口 | 对应后端服务 | 后端端口 | API 基础路径 |
|------|------|-------------|---------|------------|
| **Web 端（用户端）** | 3000 | arelore-server-user | 8081 | `/api` |
| **Web 端（管理端）** | 3001 | arelore-server-admin | 8082 | `/api` |

## 🚀 快速开始

### 1. 启动用户端 Web 应用

```bash
cd arelore-client/arelore-client-web

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm start
```

**访问地址**: http://localhost:3000

**调用后端**: `http://localhost:8081/api` (arelore-server-user)

### 2. 启动管理端 Web 应用

```bash
cd arelore-client/arelore-client-web-admin

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm start
```

**访问地址**: http://localhost:3001

**调用后端**: `http://localhost:8082/api` (arelore-server-admin)

## ⚙️ 环境配置

### Web 端 (.env)

```env
PORT=3000
REACT_APP_API_BASE_URL=http://localhost:8081/api
```

### Admin 端 (.env)

```env
PORT=3001
REACT_APP_API_BASE_URL=http://localhost:8082/api
```

## 🔧 完整启动流程

### 方案一：只启动前端（Mock 数据）

如果只需要查看前端界面，可以单独启动前端应用：

```bash
# 终端 1 - 启动用户端
cd arelore-client/arelore-client-web
npm start

# 终端 2 - 启动管理端
cd arelore-client/arelore-client-web-admin
npm start
```

### 方案二：前后端一起启动（推荐）

完整的开发环境需要同时启动前端和后端服务：

```bash
# 终端 1 - 启动用户服务
cd arelore-server/arelore-server-user
mvn spring-boot:run

# 终端 2 - 启动管理员服务
cd arelore-server/arelore-server-admin
mvn spring-boot:run

# 终端 3 - 启动用户端 Web
cd arelore-client/arelore-client-web
npm start

# 终端 4 - 启动管理端 Web
cd arelore-client/arelore-client-web-admin
npm start
```

**启动顺序建议**：
1. 先启动后端服务（等待服务完全启动）
2. 再启动前端应用

## 📁 项目结构

```
arelore-client/
├── arelore-client-web/              # 用户端 Web 应用（端口 3000）
│   ├── .env                        # 环境配置（端口 3000 + API 地址 8081）
│   ├── src/
│   │   ├── api/                    # API 接口封装
│   │   │   └── user.js            # 用户相关 API
│   │   ├── utils/                  # 工具类
│   │   │   └── request.js         # HTTP 请求封装
│   │   └── ...
│   └── package.json
│
└── arelore-client-web-admin/       # 管理端 Web 应用（端口 3001）
    ├── .env                        # 环境配置（端口 3001 + API 地址 8082）
    ├── src/
    │   ├── api/                    # API 接口封装
    │   │   └── admin.js           # 管理员相关 API
    │   ├── utils/                  # 工具类
    │   │   └── request.js         # HTTP 请求封装
    │   └── ...
    └── package.json
```

## 🔐 Token 管理

### Web 端（用户端）
- Token 存储位置：`localStorage.token`
- Token 过期处理：自动跳转到登录页

### Admin 端（管理端）
- Token 存储位置：`localStorage.adminToken`
- Token 过期处理：自动跳转到登录页

**注意**: 两个应用的 Token 是分开存储的，互不影响。

## 🌐 API 调用示例

### Web 端调用用户服务

```javascript
import { userApi } from '@/api/user';

// 获取用户列表
const response = await userApi.getList({
  pageNum: 1,
  pageSize: 10
});

// response.data 包含返回的数据
```

### Admin 端调用管理员服务

```javascript
import { adminApi } from '@/api/admin';

// 获取仪表盘数据
const response = await adminApi.getDashboard();

// 获取用户列表
const userList = await adminApi.getUserList({
  pageNum: 1,
  pageSize: 10
});
```

## 🛠️ 构建生产版本

### Web端

```bash
cd arelore-client/arelore-client-web
npm run build
# 输出目录：build/
```

### Admin 端

```bash
cd arelore-client/arelore-client-web-admin
npm run build
# 输出目录：build/
```

## ⚠️ 常见问题

### Q1: 端口被占用怎么办？

修改 `.env` 文件中的 `PORT` 值：

```env
PORT=3002  # 改为其他可用端口
```

### Q2: 如何修改后端 API 地址？

修改 `.env` 文件中的 `REACT_APP_API_BASE_URL`：

```env
REACT_APP_API_BASE_URL=http://your-server-ip:8081/api
```

### Q3: 跨域问题如何解决？

开发环境下，React 开发服务器会自动处理跨域。如果遇到跨域问题，可以在 `package.json` 中添加代理配置：

```json
{
  "proxy": "http://localhost:8081"
}
```

### Q4: 为什么两个前端要使用不同端口？

1. **独立部署**: 用户端和管理端可以独立部署和更新
2. **安全隔离**: 管理端可以有更严格的安全策略
3. **性能优化**: 可以针对不同应用做独立的性能优化
4. **团队协作**: 不同团队可以并行开发，互不干扰

## 📝 总结

**核心要点**:
- ✅ Web 端（用户端）：端口 3000 → 调用用户服务（8081）
- ✅ Admin 端（管理端）：端口 3001 → 调用管理员服务（8082）
- ✅ 两个应用完全独立，可以同时运行
- ✅ Token 分开存储，互不影响
- ✅ 统一的 API 请求封装，便于维护
