# 🧪 前后端接口联调测试指南

## 📋 测试目标

验证前端应用能否成功调用后端服务接口，确保前后端打通。

## 🚀 快速测试步骤

### 第一步：启动后端服务

**打开 2 个终端窗口：**

```bash
# 终端 1 - 启动用户服务（端口 8081）
cd /Users/huang/Documents/Workspaces/arelore/arelore-server/arelore-server-user
mvn spring-boot:run

# 终端 2 - 启动管理员服务（端口 8082）
cd /Users/huang/Documents/Workspaces/arelore/arelore-server/arelore-server-admin
mvn spring-boot:run
```

**等待服务完全启动**，看到类似以下日志表示启动成功：
```
Started UserServiceApplication in X.XXX seconds
Started AdminServiceApplication in X.XXX seconds
```

### 第二步：启动前端应用

**打开另外 2 个终端窗口：**

```bash
# 终端 3 - 启动用户端 Web（端口 3000）
cd /Users/huang/Documents/Workspaces/arelore/arelore-client/arelore-client-web
npm start

# 终端 4 - 启动管理端 Web（端口 3001）
cd /Users/huang/Documents/Workspaces/arelore/arelore-client/arelore-client-web-admin
npm start
```

**等待浏览器自动打开：**
- 用户端：http://localhost:3000
- 管理端：http://localhost:3001

## ✅ 预期结果

### 用户端 Web（端口 3000）

访问 http://localhost:3000 后应该看到：

1. **统计卡片显示数据**
   - 总用户数：应该有数字显示
   - 今日新增：随机数字
   - 产品数量：随机数字

2. **用户列表表格**
   - 显示从后端获取的用户数据
   - 包含用户名、邮箱、状态等字段

3. **绿色提示框**
   - 显示 "✅ 前端已成功连接到后端服务"
   - 如果看到 "加载数据成功" 的提示，说明接口调用成功！

### 管理端 Web（端口 3001）

访问 http://localhost:3001 后应该看到：

1. **控制台统计数据**
   - 总用户数：12580（来自后端）
   - 今日新增：256（来自后端）
   - 消息总数：89654（来自后端）
   - 系统状态：正常（来自后端）

2. **用户列表表格**
   - 显示从后端获取的用户数据

3. **绿色提示框**
   - 显示 "✅ 管理端已成功连接到后端服务"
   - 如果看到数据，说明前后端已打通！

## 🔍 故障排查

### 问题 1：前端提示 "加载数据失败"

**可能原因：**
- 后端服务未启动
- 端口被占用
- 跨域问题

**解决方案：**
```bash
# 检查后端服务是否正常运行
curl http://localhost:8081/api/user/list -X POST \
  -H "Content-Type: application/json" \
  -d '{"pageNum":1,"pageSize":10}'
```

### 问题 2：页面空白或报错

**检查浏览器控制台（F12）：**
- 查看是否有 JavaScript 错误
- 查看 Network 标签页中的请求状态

**常见错误及解决：**

1. **CORS Error（跨域错误）**
   ```
   Access to XMLHttpRequest at 'http://localhost:8081' 
   from origin 'http://localhost:3000' has been blocked by CORS policy
   ```
   
   **解决**：这是正常的，React 开发服务器会自动处理跨域。如果仍然报错，在 `package.json` 中添加：
   ```json
   {
     "proxy": "http://localhost:8081"
   }
   ```

2. **Network Error**
   - 检查后端服务是否启动
   - 检查防火墙设置
   - 确认端口号正确

### 问题 3：数据为空或显示 "暂无数据"

**检查后端接口返回：**

```bash
# 测试用户服务接口
curl -X POST http://localhost:8081/api/user/list \
  -H "Content-Type: application/json" \
  -d '{"pageNum":1,"pageSize":10}'

# 测试管理员服务接口
curl -X POST http://localhost:8082/api/admin/dashboard
```

**预期返回：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [...],
    "total": 100
  },
  "timestamp": 1234567890
}
```

## 📊 接口测试命令

### 手动测试后端接口

**1. 测试用户服务**

```bash
# 获取用户列表
curl -X POST http://localhost:8081/api/user/list \
  -H "Content-Type: application/json" \
  -d '{"pageNum":1,"pageSize":10}'

# 获取用户详情
curl -X POST http://localhost:8081/api/user/detail \
  -H "Content-Type: application/json" \
  -d '{"id":"1"}'
```

**2. 测试管理员服务**

```bash
# 获取仪表盘数据
curl -X POST http://localhost:8082/api/admin/dashboard

# 获取用户列表
curl -X POST http://localhost:8082/api/admin/users \
  -H "Content-Type: application/json" \
  -d '{"pageNum":1,"pageSize":10}'
```

## 🎯 测试检查清单

- [ ] 后端用户服务启动成功（端口 8081）
- [ ] 后端管理员服务启动成功（端口 8082）
- [ ] 前端用户端启动成功（端口 3000）
- [ ] 前端管理端启动成功（端口 3001）
- [ ] 用户端显示统计数据
- [ ] 用户端显示用户列表
- [ ] 管理端显示仪表盘数据
- [ ] 管理端显示用户列表
- [ ] 两个前端都显示绿色的 "API 连接测试" 成功提示
- [ ] 浏览器控制台无错误
- [ ] Network 面板中请求状态为 200

## 💡 成功标志

如果你看到以下内容，说明前后端已经完全打通：

✅ 用户端显示真实用户数据  
✅ 管理端显示仪表盘统计  
✅ 点击"刷新数据"按钮能重新加载数据  
✅ 浏览器 Network 面板中看到成功的 POST 请求  
✅ 控制台中没有错误信息  

## 📝 下一步

测试成功后，可以继续：

1. 实现完整的业务功能
2. 添加更多的 API 接口调用
3. 实现用户登录和权限控制
4. 优化界面和交互体验

祝你测试顺利！🎉
