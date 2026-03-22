#!/bin/bash

# JDK 版本切换验证脚本
# 功能：检查当前环境并验证项目是否能正常编译

echo "=========================================="
echo "JDK 17 配置验证"
echo "=========================================="
echo ""

# 检查 Java 版本
echo "检查 Java 版本..."
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
echo "当前 Java 版本：$JAVA_VERSION"

# 提取主版本号
JAVA_MAJOR_VERSION=$(echo $JAVA_VERSION | cut -d'.' -f1)

if [ "$JAVA_MAJOR_VERSION" != "17" ]; then
    echo "⚠️  警告：当前不是 Java 17，而是 Java $JAVA_MAJOR_VERSION"
    echo ""
    echo "请安装并切换到 Java 17:"
    echo ""
    echo "macOS (使用 SDKMAN):"
    echo "  sdk install java 17.0.x-tem"
    echo "  sdk use java 17.0.x-tem"
    echo ""
    echo "macOS (手动安装):"
    echo "  1. 下载：https://adoptium.net/"
    echo "  2. 安装后，在 IDEA 中配置 Project SDK"
    echo ""
    echo "Linux/CentOS:"
    echo "  sudo yum install java-17-openjdk -y"
    echo ""
    exit 1
else
    echo "✅ Java 版本正确"
fi

echo ""

# 检查 Maven 版本
echo "检查 Maven 版本..."
MVN_VERSION=$(mvn -version 2>&1 | head -n 1 | awk '{print $3}')
echo "当前 Maven 版本：$MVN_VERSION"

# 简单的版本比较（假设版本号格式为 x.y.z）
MVN_MAJOR=$(echo $MVN_VERSION | cut -d'.' -f1)
if [ "$MVN_MAJOR" -lt 3 ]; then
    echo "❌ Maven 版本过低，需要 >= 3.6"
    exit 1
else
    echo "✅ Maven 版本兼容"
fi

echo ""

# 进入项目目录
cd "$(dirname "$0")"

# 清理旧的构建
echo "清理旧的构建..."
mvn clean -q

# 尝试编译
echo "编译项目 (JDK 17)..."
mvn clean compile -DskipTests -q

if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "✅ 编译成功！JDK 17 配置正确"
    echo "=========================================="
    echo ""
    echo "现在可以运行："
    echo "  ./install-deps.sh      # 安装依赖"
    echo "  cd arelore-server-user && ./start.sh   # 启动用户服务"
    echo "  cd ../arelore-server-admin && ./start.sh   # 启动管理员服务"
    echo ""
else
    echo ""
    echo "=========================================="
    echo "❌ 编译失败"
    echo "=========================================="
    echo ""
    echo "可能的原因:"
    echo "  1. IDE 使用了错误的 JDK 版本"
    echo "  2. Maven 缓存问题"
    echo "  3. 代码中存在不兼容 JDK 17 的特性"
    echo ""
    echo "解决方案:"
    echo "  1. 在 IDE 中重新导入 Maven 项目"
    echo "  2. 清理 Maven 缓存：rm -rf ~/.m2/repository/com/arelore"
    echo "  3. 检查 IDE 的 Project Structure 设置"
    exit 1
fi
