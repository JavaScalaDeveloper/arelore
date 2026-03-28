# Arelore Server User Module

## 模块介绍

该模块为C端应用提供用户相关的核心服务，包括用户认证、敏感信息检测、文件处理等功能。

## 主要功能

1. **用户认证服务**
   - 微信扫码登录
   - 用户信息管理

2. **敏感信息检测服务**
   - 文本敏感信息检测与脱敏
   - 支持多种敏感类型：身份证、银行卡、手机号、邮箱、姓名等

3. **文件检测服务**
   - 支持多种文件类型的内容提取和敏感信息检测
   - 支持的文件类型：
     - 图片文件（.jpg, .jpeg, .png, .gif, .bmp）
     - Office文档（.doc, .docx, .ppt, .pptx, .xls, .xlsx）
     - PDF文件（.pdf）
     - 文本文件（.txt）
     - HTML文件（.html, .htm）
     - Markdown文件（.md）
     - JSON文件（.json）
     - 日志文件（.log）
     - SQL文件（.sql）
     - RTF文件（.rtf）

4. **设计模式应用**
   - 策略模式：用于文件处理和敏感信息检测
   - 工厂模式：用于创建不同类型的处理策略

## 技术栈

- Spring Boot
- Maven
- Apache POI（处理Office文档）
- PDFBox（处理PDF文件）
- Tesseract OCR（图片文字识别）
- Hutool（工具类库）

## 运行说明

1. 编译项目：
```bash
mvn clean install
```

2. 运行服务：
```bash
java -jar target/arelore-server-user-1.0.0-SNAPSHOT.jar
```

3. 或使用启动脚本：
```bash
./start.sh
```

## OCR环境配置（可选）

如果需要使用图片OCR功能，建议安装Tesseract OCR：

### Mac系统

**方法：使用Homebrew（推荐）**
```bash
# 如果Homebrew版本过旧，先更新
brew update

# 安装Tesseract和中文语言包
brew install tesseract tesseract-lang
```

### CentOS系统
```bash
# 安装Tesseract
sudo yum install -y tesseract

# 手动下载中文语言包（如果系统没有提供）
sudo mkdir -p /usr/share/tesseract-ocr/4.00/tessdata/
sudo wget -O /usr/share/tesseract-ocr/4.00/tessdata/chi_sim.traineddata https://github.com/tesseract-ocr/tessdata/raw/main/chi_sim.traineddata
```

## 注意事项

- Tesseract OCR为可选依赖，若系统未安装，图片文件处理会返回降级内容
- 请确保服务器有足够的内存和CPU资源处理大文件
- 建议设置合理的文件大小限制，避免内存溢出