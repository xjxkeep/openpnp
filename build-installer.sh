#!/bin/bash

# OpenPnP 安装程序构建脚本
# 基于 install4j 配置文件

set -e

echo "=== OpenPnP 安装程序构建脚本 ==="

# 检查必要的工具
check_requirements() {
    echo "检查构建要求..."
    
    # 检查Java
    if ! command -v java &> /dev/null; then
        echo "错误: 需要安装Java"
        exit 1
    fi
    
    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        echo "错误: 需要安装Maven"
        exit 1
    fi
    
    # 检查install4j (可选)
    if ! command -v install4jc &> /dev/null; then
        echo "警告: install4jc 未找到，将跳过安装程序构建"
        echo "请从 https://www.ej-technologies.com/download/install4j/files 下载install4j"
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
        echo "构建安装程序..."
        
        # 创建安装程序目录
        mkdir -p installers
        
        # 构建安装程序
        install4jc -r "$VERSION" \
                   -d installers \
                   -D mediaFileVersion="$VERSION" \
                   OpenPnP.install4j
        
        echo "✓ 安装程序构建完成"
        echo "安装程序位置: installers/"
    else
        echo "跳过安装程序构建 (install4jc 不可用)"
    fi
}

# 创建便携式版本
create_portable() {
    echo "创建便携式版本..."
    
    PORTABLE_DIR="OpenPnP-Portable-$VERSION"
    mkdir -p "$PORTABLE_DIR"
    
    # 复制应用程序文件
    cp -r target/appassembler/* "$PORTABLE_DIR/"
    cp -r samples "$PORTABLE_DIR/"
    cp LICENSE.txt "$PORTABLE_DIR/"
    cp CHANGES.md "$PORTABLE_DIR/"
    cp README.md "$PORTABLE_DIR/"
    
    # 创建启动脚本
    cat > "$PORTABLE_DIR/start.sh" << 'EOF'
#!/bin/bash
cd "$(dirname "$0")"
./bin/openpnp
EOF
    chmod +x "$PORTABLE_DIR/start.sh"
    
    # 创建Windows批处理文件
    cat > "$PORTABLE_DIR/start.bat" << 'EOF'
@echo off
cd /d "%~dp0"
bin\openpnp.bat
EOF
    
    echo "✓ 便携式版本创建完成: $PORTABLE_DIR"
}

# 创建压缩包
create_archive() {
    echo "创建压缩包..."
    
    # 创建tar.gz压缩包
    tar -czf "OpenPnP-$VERSION.tar.gz" "OpenPnP-Portable-$VERSION"
    
    # 创建zip压缩包 (如果在Windows上)
    if command -v zip &> /dev/null; then
        zip -r "OpenPnP-$VERSION.zip" "OpenPnP-Portable-$VERSION"
    fi
    
    echo "✓ 压缩包创建完成"
}

# 显示构建结果
show_results() {
    echo ""
    echo "=== 构建完成 ==="
    echo "版本: $VERSION"
    echo ""
    echo "生成的文件:"
    
    if [ -d "installers" ]; then
        echo "- 安装程序: installers/"
        ls -la installers/
    fi
    
    if [ -d "OpenPnP-Portable-$VERSION" ]; then
        echo "- 便携式版本: OpenPnP-Portable-$VERSION/"
    fi
    
    if [ -f "OpenPnP-$VERSION.tar.gz" ]; then
        echo "- 压缩包: OpenPnP-$VERSION.tar.gz"
    fi
    
    if [ -f "OpenPnP-$VERSION.zip" ]; then
        echo "- ZIP包: OpenPnP-$VERSION.zip"
    fi
    
    echo ""
    echo "使用方法:"
    echo "1. 安装程序: 运行 installers/ 中的安装程序"
    echo "2. 便携式版本: 解压并运行 start.sh (Linux/Mac) 或 start.bat (Windows)"
}

# 主函数
main() {
    check_requirements
    build_java_app
    get_version
    build_installer
    create_portable
    create_archive
    show_results
}

# 运行主函数
main "$@" 