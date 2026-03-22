# 🚀 Arelore Server 快速启动指南

## 📁 脚本位置

### 用户服务 (User Service)
- **脚本**: `arelore-server-user/start.sh`
- **端口**: 8081
- **日志**: `/var/log/arelore/arelore-server-user.log`

### 管理员服务 (Admin Service)
- **脚本**: `arelore-server-admin/start.sh`
- **端口**: 8082
- **日志**: `/var/log/arelore/arelore-server-admin.log`

## ⚡ 快速开始

### 1️⃣ 添加执行权限

```bash
chmod +x arelore-server-user/start.sh
chmod +x arelore-server-admin/start.sh
chmod +x cleanup-logs.sh
chmod +x install-deps.sh
```

### 2️⃣ 首次运行 - 安装依赖

**方式一：使用自动安装脚本（推荐）**

```bash
cd arelore-server
./install-deps.sh
```

**方式二：手动安装**

```bash
cd arelore-server
mvn clean install -DskipTests
```

**方式三：在启动时自动安装**

启动脚本会自动检测，如果父模块未安装，会自动先安装依赖。

### 3️⃣ 创建日志目录并授权

```bash
sudo mkdir -p /var/log/arelore
sudo chmod 777 /var/log/arelore
```

### 4️⃣ 启动服务

```bash
# 启动用户服务
cd arelore-server-user
./start.sh

# 启动管理员服务
cd ../arelore-server-admin
./start.sh
```

### 4️⃣ 查看日志

```bash
# 实时查看用户服务日志
tail -f /var/log/arelore/arelore-server-user.log

# 实时查看管理员服务日志
tail -f /var/log/arelore/arelore-server-admin.log
```

### 5️⃣ 停止服务

```bash
# 停止用户服务
kill $(cat /var/log/arelore/arelore-server-user.pid)

# 停止管理员服务
kill $(cat /var/log/arelore/arelore-server-admin.pid)
```

## 🔄 配置日志自动过期（CentOS）

### 方法一：使用 logrotate（推荐）⭐

```bash
# 复制配置文件到系统目录
sudo cp arelore-logrotate /etc/logrotate.d/arelore

# 测试配置
sudo logrotate -d /etc/logrotate.d/arelore

# 强制轮转一次
sudo logrotate -f /etc/logrotate.d/arelore
```

**配置说明**：
- 每天自动轮转日志
- 保留 30 天
- 自动压缩旧日志
- 无需手动干预

### 方法二：使用定时任务

```bash
# 编辑 crontab
crontab -e

# 添加每天凌晨 2 点清理日志的任务
0 2 * * * /path/to/arelore-server/cleanup-logs.sh
```

## 📊 完整部署流程

```bash
#!/bin/bash
# deploy.sh - 一键部署脚本

echo "=========================================="
echo "Arelore Server 部署"
echo "=========================================="

# 1. 创建日志目录
echo "创建日志目录..."
sudo mkdir -p /var/log/arelore
sudo chmod 777 /var/log/arelore

# 2. 添加脚本权限
echo "添加脚本权限..."
chmod +x arelore-server-user/start.sh
chmod +x arelore-server-admin/start.sh
chmod +x install-deps.sh

# 3. 安装依赖（首次运行）
echo "安装依赖..."
./install-deps.sh

if [ $? -ne 0 ]; then
    echo "❌ 依赖安装失败，请检查网络和 Maven 配置"
    exit 1
fi

# 4. 启动用户服务
echo "启动用户服务..."
cd arelore-server-user
./start.sh

# 等待服务启动
sleep 10

# 5. 启动管理员服务
echo "启动管理员服务..."
cd ../arelore-server-admin
./start.sh

echo ""
echo "=========================================="
echo "✅ 部署完成!"
echo "=========================================="
echo ""
echo "服务状态:"
echo "  用户服务：http://localhost:8081"
echo "  管理员服务：http://localhost:8082"
echo ""
echo "日志文件:"
echo "  /var/log/arelore/arelore-server-user.log"
echo "  /var/log/arelore/arelore-server-admin.log"
echo ""
echo "查看日志：tail -f /var/log/arelore/*.log"
```

## 🔍 常用命令

```bash
# 查看服务是否运行
ps aux | grep arelore

# 查看端口占用
netstat -tlnp | grep 8081
netstat -tlnp | grep 8082

# 查看进程 PID
cat /var/log/arelore/arelore-server-user.pid
cat /var/log/arelore/arelore-server-admin.pid

# 查看日志最后 100 行
tail -n 100 /var/log/arelore/arelore-server-user.log

# 搜索错误日志
grep "ERROR" /var/log/arelore/arelore-server-user.log

# 查看服务启动时间
grep "Started.*Application" /var/log/arelore/arelore-server-user.log
```

## ⚠️ 注意事项

1. **日志目录权限**: 确保 `/var/log/arelore` 目录可写
2. **端口检查**: 确保 8081 和 8082 端口未被占用
3. **Java 版本**: 需要 Java 21+
4. **Maven**: 需要 Maven 3.8+
5. **内存**: 建议至少 2GB 可用内存

## 🎯 生产环境建议

### 使用 systemd 管理服务（可选）

创建服务文件 `/etc/systemd/system/arelore-user.service`:

```ini
[Unit]
Description=Arelore User Service
After=syslog.target network.target

[Service]
Type=forking
PIDFile=/var/log/arelore/arelore-server-user.pid
ExecStart=/path/to/arelore-server-user/start.sh
ExecStop=/bin/kill $(cat /var/log/arelore/arelore-server-user.pid)
Restart=always
User=root
Group=root

[Install]
WantedBy=multi-user.target
```

然后：

```bash
# 重载 systemd
sudo systemctl daemon-reload

# 启用服务
sudo systemctl enable arelore-user

# 启动服务
sudo systemctl start arelore-user

# 查看状态
sudo systemctl status arelore-user
```

---

**就这么简单！** 🎉
