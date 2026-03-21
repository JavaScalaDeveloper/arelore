# Arelore Web Admin

Arelore 管理后台系统（Web 端），基于 React + Ant Design 构建。

## 技术栈

- **React**: 18.2.0
- **Ant Design**: 5.12.0
- **React Router**: 6.20.0
- **Axios**: 1.6.2

## 功能模块

- ✅ **控制台** - 数据统计和概览
- ✅ **用户管理** - 用户列表、添加、编辑、删除
- ✅ **系统设置** - 基础配置、邮件设置
- 🔲 **权限管理** - 角色和权限分配（待开发）
- 🔲 **内容管理** - 内容审核和管理（待开发）
- 🔲 **日志监控** - 系统日志和操作记录（待开发）

## 快速开始

### 安装依赖

```bash
cd arelore-client/arelore-client-web-admin
npm install
```

### 启动开发服务器

```bash
npm start
```

访问 http://localhost:3001（需要修改端口，避免与用户端冲突）

### 构建生产版本

```bash
npm run build
```

## 项目结构

```
arelore-client-web-admin/
├── public/                      # 静态资源
│   ├── index.html              # HTML 模板
│   └── manifest.json           # PWA 配置
├── src/
│   ├── pages/                  # 页面组件
│   │   ├── Layout.js          # 布局组件
│   │   ├── Login.js           # 登录页
│   │   ├── Dashboard.js       # 控制台
│   │   ├── UserManagement.js  # 用户管理
│   │   └── SystemSettings.js  # 系统设置
│   ├── App.js                  # 主应用组件
│   ├── index.js                # 入口文件
│   └── index.css               # 全局样式
└── package.json                # 依赖配置
```

## API 接口

所有 API 请求都通过 Axios 发送到后端服务：

- **基础 URL**: `http://localhost:8080/api/admin` (开发环境)
- **认证方式**: JWT Token
- **数据格式**: JSON

## 与用户端的区别

| 特性 | 用户端 (web) | 管理端 (web-admin) |
|------|-------------|-------------------|
| 目标用户 | 普通用户 | 管理员 |
| 端口 | 3000 | 3001 |
| 功能 | 浏览、使用产品 | 管理、配置、监控 |
| 权限 | 普通权限 | 高级权限 |
| 部署 | 独立部署 | 独立部署 |

## 下一步计划

- [ ] 集成真实的后端 API
- [ ] 实现完整的权限控制
- [ ] 添加数据可视化图表
- [ ] 实现操作日志功能
- [ ] 优化移动端适配
- [ ] 添加批量操作功能

## 注意事项

1. **端口配置**: 管理端应使用不同的端口（建议 3001），避免与用户端（3000）冲突
2. **权限控制**: 需要实现完整的路由守卫和权限验证
3. **安全性**: 管理端应该有更严格的安全措施
4. **独立部署**: 管理端和用户端应该独立打包和部署
