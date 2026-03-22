#!/bin/bash

# Arelore Server 依赖安装脚本
# 功能：安装父模块和所有依赖

echo "=========================================="
echo "Arelore Server 依赖安装"
echo "=========================================="
echo ""

# 进入脚本所在目录
cd "$(dirname "$0")"

# 检查 Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven 未安装，请先安装 Maven"
    exit 1
fi

echo "Maven 版本:"
mvn -version
echo ""

# 清理旧的构建
echo "清理旧的构建..."
mvn clean -q

# 安装父模块和所有子模块依赖
echo "安装父模块和依赖..."
echo "这可能需要几分钟，取决于网络速度..."
echo ""

mvn clean install -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 依赖安装完成!"
    echo ""
    echo "现在可以运行启动脚本了:"
    echo "  cd arelore-server-user && ./start.sh"
    echo "  cd arelore-server-admin && ./start.sh"
else
    echo ""
    echo "❌ 依赖安装失败"
    echo "请检查:"
    echo "  1. 网络连接是否正常"
    echo "  2. Maven 配置是否正确 (settings.xml)"
    echo "  3. 是否使用了正确的 Java 版本 (Java 21+)"
    exit 1
fi
