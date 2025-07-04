# OpenPnP Linux 打包指南

## 概述

本文档介绍如何在Linux系统上打包OpenPnP应用程序。Linux打包与macOS相比有一些特殊考虑。

## 系统要求

### 基本要求
- **Java 11或更高版本**
- **Maven 3.6+**
- **Git**
- **至少2GB可用内存**

### Linux特定要求
- **OpenCV开发库**
- **JNA库**
- **其他原生库依赖**

## 安装依赖

### Ubuntu/Debian系统
```bash
# 安装Java
sudo apt-get update
sudo apt-get install openjdk-11-jdk

# 安装Maven
sudo apt-get install maven

# 安装OpenCV开发库
sudo apt-get install libopencv-dev

# 安装其他依赖
sudo apt-get install build-essential
```

### CentOS/RHEL系统
```bash
# 安装Java
sudo yum install java-11-openjdk-devel

# 安装Maven
sudo yum install maven

# 安装OpenCV开发库
sudo yum install opencv-devel

# 安装其他依赖
sudo yum groupinstall "Development Tools"
```

### Arch Linux
```bash
# 安装Java
sudo pacman -S jdk11-openjdk

# 安装Maven
sudo pacman -S maven

# 安装OpenCV
sudo pacman -S opencv
```

## 打包步骤

### 1. 克隆代码
```bash
git clone https://github.com/openpnp/openpnp.git
cd openpnp
```

### 2. 运行Linux打包脚本
```bash
# 给脚本执行权限
chmod +x build-installer-linux.sh

# 运行打包脚本
./build-installer-linux.sh
```

### 3. 脚本功能说明

Linux打包脚本 (`build-installer-linux.sh`) 包含以下功能：

#### 操作系统检测
- 自动检测Linux发行版
- 识别Ubuntu、Debian、CentOS等系统
- 提供针对性的依赖检查

#### 依赖检查
- 检查Java版本和安装
- 检查Maven可用性
- 检查OpenCV等系统库
- 提供安装指导

#### 构建优化
- 针对Linux系统优化构建参数
- 生成Linux特定的启动脚本
- 创建桌面快捷方式脚本

#### 打包输出
- 生成Linux标准的tar.gz压缩包
- 创建跨平台兼容的zip包
- 包含Linux特定的安装和卸载脚本

## 生成的文件

### 便携式版本
```
OpenPnP-Portable-{版本号}/
├── bin/                    # 可执行文件
├── lib/                    # 依赖库
├── samples/                # 示例文件
├── start.sh               # Linux启动脚本
├── start.bat              # Windows启动脚本
├── install-desktop.sh     # 桌面快捷方式安装脚本
├── uninstall.sh           # 卸载脚本
├── README.md              # 说明文档
├── LICENSE.txt            # 许可证
└── CHANGES.md             # 变更日志
```

### 压缩包
- `OpenPnP-Linux-{版本号}.tar.gz` - Linux标准压缩包
- `OpenPnP-Linux-{版本号}.zip` - 跨平台压缩包

## 使用方法

### 便携式版本
```bash
# 解压
tar -xzf OpenPnP-Linux-{版本号}.tar.gz

# 进入目录
cd OpenPnP-Portable-{版本号}

# 运行应用程序
./start.sh

# 创建桌面快捷方式 (可选)
./install-desktop.sh

# 卸载 (可选)
./uninstall.sh
```

### 系统安装 (如果有install4j)
```bash
# 运行安装程序
./installers/OpenPnP-{版本号}-linux-installer.run

# 或使用包管理器安装
sudo dpkg -i openpnp_{版本号}_amd64.deb  # Ubuntu/Debian
sudo rpm -i openpnp-{版本号}.x86_64.rpm  # CentOS/RHEL
```

## 故障排除

### 常见问题

#### 1. Java版本问题
```bash
# 检查Java版本
java -version

# 如果版本低于11，安装新版本
sudo apt-get install openjdk-11-jdk  # Ubuntu/Debian
sudo yum install java-11-openjdk-devel  # CentOS/RHEL
```

#### 2. OpenCV库缺失
```bash
# 检查OpenCV库
ldconfig -p | grep opencv

# 安装OpenCV
sudo apt-get install libopencv-dev  # Ubuntu/Debian
sudo yum install opencv-devel  # CentOS/RHEL
```

#### 3. 权限问题
```bash
# 确保脚本有执行权限
chmod +x start.sh
chmod +x install-desktop.sh
chmod +x uninstall.sh
```

#### 4. 图形界面问题
```bash
# 检查X11转发 (如果通过SSH运行)
export DISPLAY=:0

# 检查OpenGL支持
glxinfo | grep "OpenGL version"
```

### 调试模式
```bash
# 启用详细日志
export OPENPNP_DEBUG=true
./start.sh

# 检查Java选项
java -version --add-opens=java.base/java.lang=ALL-UNNAMED
```

## 跨平台兼容性

### 与macOS的差异
1. **文件路径**: Linux使用正斜杠 `/`，macOS也支持
2. **权限系统**: Linux有更严格的权限控制
3. **库依赖**: Linux需要更多的系统库
4. **包管理**: Linux使用不同的包管理器

### 与Windows的差异
1. **可执行文件**: Linux使用ELF格式，Windows使用PE格式
2. **启动脚本**: Linux使用bash脚本，Windows使用批处理文件
3. **库加载**: Linux使用LD_LIBRARY_PATH，Windows使用PATH

## 高级配置

### 自定义Java选项
编辑 `start.sh` 文件中的 `JAVA_OPTS` 变量：
```bash
export JAVA_OPTS="--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.desktop/java.awt=ALL-UNNAMED --add-opens=java.desktop/java.awt.color=ALL-UNNAMED -Xmx2g -Xms512m"
```

### 系统集成
```bash
# 创建系统级安装
sudo cp -r OpenPnP-Portable-{版本号} /opt/openpnp
sudo ln -s /opt/openpnp/start.sh /usr/local/bin/openpnp

# 创建桌面图标
sudo cp OpenPnP.desktop /usr/share/applications/
```

## 性能优化

### 内存设置
```bash
# 在start.sh中调整内存设置
export JAVA_OPTS="$JAVA_OPTS -Xmx4g -Xms1g"
```

### 图形性能
```bash
# 启用硬件加速
export JAVA_OPTS="$JAVA_OPTS -Dsun.java2d.opengl=true"
```

## 安全考虑

### 文件权限
```bash
# 设置适当的文件权限
chmod 755 start.sh
chmod 644 *.jar
chmod 644 *.properties
```

### 沙盒运行
```bash
# 在受限环境中运行
firejail --net=none --private ./start.sh
```

## 自动化部署

### CI/CD集成
```bash
# 在GitHub Actions中使用
- name: Build OpenPnP for Linux
  run: |
    chmod +x build-installer-linux.sh
    ./build-installer-linux.sh
```

### Docker支持
```dockerfile
FROM ubuntu:20.04
RUN apt-get update && apt-get install -y \
    openjdk-11-jdk \
    maven \
    libopencv-dev
COPY . /app
WORKDIR /app
RUN ./build-installer-linux.sh
```

## 总结

Linux打包主要需要考虑：
1. **系统依赖检查** - 确保所有必要的库都已安装
2. **权限管理** - 正确设置文件权限
3. **启动脚本优化** - 针对Linux系统优化
4. **桌面集成** - 提供桌面快捷方式
5. **跨平台兼容** - 保持与其他平台的兼容性

通过这些步骤，你可以在Linux系统上成功打包OpenPnP应用程序。 