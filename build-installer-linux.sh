#!/bin/bash

# OpenPnP Linux 安装程序构建脚本
# 专门针对Linux系统优化

set -e

echo "=== OpenPnP Linux 安装程序构建脚本 ==="

# 检测操作系统
detect_os() {
    echo "检测操作系统..."
    
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        OS="linux"
        echo "✓ 检测到Linux系统"
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        OS="macos"
        echo "✓ 检测到macOS系统"
    else
        OS="unknown"
        echo "⚠️  未知操作系统: $OSTYPE"
    fi
    
    # 检测Linux发行版
    if [[ "$OS" == "linux" ]]; then
        if [[ -f /etc/os-release ]]; then
            . /etc/os-release
            DISTRO="$ID"
            DISTRO_VERSION="$VERSION_ID"
            echo "✓ Linux发行版: $DISTRO $DISTRO_VERSION"
        else
            DISTRO="unknown"
            echo "⚠️  无法检测Linux发行版"
        fi
    fi
}

# 检查必要的工具
check_requirements() {
    echo "检查构建要求..."
    
    # 检查Java
    if ! command -v java &> /dev/null; then
        echo "错误: 需要安装Java"
        echo "请运行: sudo apt-get install openjdk-11-jdk (Ubuntu/Debian)"
        echo "或: sudo yum install java-11-openjdk-devel (CentOS/RHEL)"
        exit 1
    fi
    
    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        echo "错误: 需要安装Maven"
        echo "请运行: sudo apt-get install maven (Ubuntu/Debian)"
        echo "或: sudo yum install maven (CentOS/RHEL)"
        exit 1
    fi
    
    # 检查必要的系统库
    if [[ "$OS" == "linux" ]]; then
        echo "检查Linux系统库..."
        
        # 检查OpenCV依赖
        if ! ldconfig -p | grep -q libopencv; then
            echo "警告: OpenCV库可能未安装"
            echo "请运行: sudo apt-get install libopencv-dev (Ubuntu/Debian)"
            echo "或: sudo yum install opencv-devel (CentOS/RHEL)"
        fi
        
        # 检查其他必要的库
        local missing_libs=()
        
        for lib in libjna libbridj libjSerialComm; do
            if ! ldconfig -p | grep -q "$lib"; then
                missing_libs+=("$lib")
            fi
        done
        
        if [[ ${#missing_libs[@]} -gt 0 ]]; then
            echo "警告: 以下库可能缺失: ${missing_libs[*]}"
            echo "这些库通常包含在Java应用程序中，但可能需要系统级安装"
        fi
    fi
    
    # 检查install4j (可选)
    if ! command -v install4jc &> /dev/null; then
        echo "警告: install4jc 未找到，将跳过安装程序构建"
        echo "请从 https://www.ej-technologies.com/download/install4j/files 下载install4j"
        echo "Linux版本: install4j_unix_*.tar.gz"
    fi
    
    echo "✓ 构建要求检查完成"
}

# 构建Java应用程序
build_java_app() {
    echo "构建Java应用程序..."
    
    # 清理之前的构建
    mvn clean
    
    # 编译和打包
    mvn package -DskipTests
    
    echo "✓ Java应用程序构建完成"
}

# 获取版本信息
get_version() {
    echo "获取版本信息..."
    
    # 从Java应用程序获取版本
    VERSION=$(java --add-opens=java.base/java.lang=ALL-UNNAMED \
                   --add-opens=java.desktop/java.awt=ALL-UNNAMED \
                   --add-opens=java.desktop/java.awt.color=ALL-UNNAMED \
                   -cp target/openpnp-gui-0.0.1-alpha-SNAPSHOT.jar:target/lib/* \
                   org.openpnp.Main --version 2>/dev/null || echo "unknown")
    
    echo "版本: $VERSION"
    echo "✓ 版本信息获取完成"
}

# 构建安装程序 (如果install4j可用)
build_installer() {
    if command -v install4jc &> /dev/null; then
        echo "构建Linux安装程序..."
        
        # 创建安装程序目录
        mkdir -p installers
        
        # 构建安装程序 - 针对Linux优化
        install4jc -r "$VERSION" \
                   -d installers \
                   -D mediaFileVersion="$VERSION" \
                   -D linuxDistro="$DISTRO" \
                   OpenPnP.install4j
        
        echo "✓ Linux安装程序构建完成"
        echo "安装程序位置: installers/"
    else
        echo "跳过安装程序构建 (install4jc 不可用)"
    fi
}

# 创建便携式版本
create_portable() {
    echo "创建Linux便携式版本..."
    
    PORTABLE_DIR="OpenPnP-Portable-$VERSION"
    mkdir -p "$PORTABLE_DIR"
    
    # 复制应用程序文件
    cp -r target/appassembler/* "$PORTABLE_DIR/"
    cp -r samples "$PORTABLE_DIR/"
    cp LICENSE.txt "$PORTABLE_DIR/"
    cp CHANGES.md "$PORTABLE_DIR/"
    cp README.md "$PORTABLE_DIR/"
    
    # 创建Linux启动脚本
    cat > "$PORTABLE_DIR/start.sh" << 'EOF'
#!/bin/bash
cd "$(dirname "$0")"

# 设置Java选项
export JAVA_OPTS="--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.desktop/java.awt=ALL-UNNAMED --add-opens=java.desktop/java.awt.color=ALL-UNNAMED"

# 检查Java版本
java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [[ "$java_version" -lt 11 ]]; then
    echo "错误: 需要Java 11或更高版本"
    echo "当前版本: $(java -version 2>&1 | head -n 1)"
    exit 1
fi

# 运行应用程序
./bin/openpnp
EOF
    chmod +x "$PORTABLE_DIR/start.sh"
    
    # 创建Windows批处理文件 (用于跨平台兼容)
    cat > "$PORTABLE_DIR/start.bat" << 'EOF'
@echo off
cd /d "%~dp0"
bin\openpnp.bat
EOF
    
    # 创建桌面快捷方式脚本
    cat > "$PORTABLE_DIR/install-desktop.sh" << 'EOF'
#!/bin/bash
# 创建桌面快捷方式

DESKTOP_DIR="$HOME/Desktop"
APP_DIR="$(cd "$(dirname "$0")" && pwd)"
ICON_PATH="$APP_DIR/bin/openpnp"

# 创建.desktop文件
cat > "$DESKTOP_DIR/OpenPnP.desktop" << DESKTOP_EOF
[Desktop Entry]
Version=1.0
Type=Application
Name=OpenPnP
Comment=Open Source SMT Pick and Place Software
Exec=$APP_DIR/start.sh
Icon=$ICON_PATH
Terminal=false
Categories=Development;Engineering;
DESKTOP_EOF

chmod +x "$DESKTOP_DIR/OpenPnP.desktop"
echo "✓ 桌面快捷方式已创建: $DESKTOP_DIR/OpenPnP.desktop"
EOF
    chmod +x "$PORTABLE_DIR/install-desktop.sh"
    
    # 创建卸载脚本
    cat > "$PORTABLE_DIR/uninstall.sh" << 'EOF'
#!/bin/bash
# 卸载脚本

echo "正在卸载OpenPnP..."

# 删除桌面快捷方式
if [[ -f "$HOME/Desktop/OpenPnP.desktop" ]]; then
    rm "$HOME/Desktop/OpenPnP.desktop"
    echo "✓ 已删除桌面快捷方式"
fi

echo "✓ 卸载完成"
echo "注意: 应用程序文件仍保留在当前目录中"
EOF
    chmod +x "$PORTABLE_DIR/uninstall.sh"
    
    echo "✓ Linux便携式版本创建完成: $PORTABLE_DIR"
}

# 创建压缩包
create_archive() {
    echo "创建Linux压缩包..."
    
    # 创建tar.gz压缩包 (Linux标准)
    tar -czf "OpenPnP-Linux-$VERSION.tar.gz" "OpenPnP-Portable-$VERSION"
    
    # 创建zip压缩包 (跨平台兼容)
    if command -v zip &> /dev/null; then
        zip -r "OpenPnP-Linux-$VERSION.zip" "OpenPnP-Portable-$VERSION"
    fi
    
    echo "✓ Linux压缩包创建完成"
}

# 创建系统包 (可选)
create_system_package() {
    if [[ "$OS" == "linux" ]]; then
        echo "创建系统包..."
        
        # 检测包管理器
        if command -v dpkg &> /dev/null; then
            echo "检测到Debian/Ubuntu系统，可以创建.deb包"
            # 这里可以添加.deb包创建逻辑
        elif command -v rpm &> /dev/null; then
            echo "检测到RPM系统，可以创建.rpm包"
            # 这里可以添加.rpm包创建逻辑
        else
            echo "未检测到支持的包管理器"
        fi
    fi
}

# 显示构建结果
show_results() {
    echo ""
    echo "=== Linux构建完成 ==="
    echo "版本: $VERSION"
    echo "操作系统: $OS"
    if [[ "$OS" == "linux" ]]; then
        echo "发行版: $DISTRO $DISTRO_VERSION"
    fi
    echo ""
    echo "生成的文件:"
    
    if [ -d "installers" ]; then
        echo "- 安装程序: installers/"
        ls -la installers/
    fi
    
    if [ -d "OpenPnP-Portable-$VERSION" ]; then
        echo "- 便携式版本: OpenPnP-Portable-$VERSION/"
    fi
    
    if [ -f "OpenPnP-Linux-$VERSION.tar.gz" ]; then
        echo "- Linux压缩包: OpenPnP-Linux-$VERSION.tar.gz"
    fi
    
    if [ -f "OpenPnP-Linux-$VERSION.zip" ]; then
        echo "- 跨平台ZIP包: OpenPnP-Linux-$VERSION.zip"
    fi
    
    echo ""
    echo "Linux使用方法:"
    echo "1. 解压: tar -xzf OpenPnP-Linux-$VERSION.tar.gz"
    echo "2. 运行: cd OpenPnP-Portable-$VERSION && ./start.sh"
    echo "3. 创建桌面快捷方式: ./install-desktop.sh"
    echo "4. 卸载: ./uninstall.sh"
    echo ""
    echo "系统要求:"
    echo "- Java 11或更高版本"
    echo "- 至少2GB可用内存"
    echo "- 支持OpenGL的图形卡"
}

# 主函数
main() {
    detect_os
    check_requirements
    build_java_app
    get_version
    build_installer
    create_portable
    create_archive
    create_system_package
    show_results
}

# 运行主函数
main "$@" 