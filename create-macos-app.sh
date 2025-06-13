#!/bin/bash

# 设置变量
APP_NAME="OpenPnP"
BUILD_DIR="build"
APP_DIR="$BUILD_DIR/$APP_NAME.app"
CONTENTS_DIR="$APP_DIR/Contents"
MACOS_DIR="$CONTENTS_DIR/MacOS"
RESOURCES_DIR="$CONTENTS_DIR/Resources"
JAVA_DIR="$CONTENTS_DIR/Java"
ASSEMBLER_DIR="target/appassembler"

# 检查 appassembler 目录是否存在
if [ ! -d "$ASSEMBLER_DIR" ]; then
    echo "错误: $ASSEMBLER_DIR 目录不存在"
    echo "请先运行: mvn clean package"
    exit 1
fi

# 创建构建目录
echo "创建构建目录..."
mkdir -p "$BUILD_DIR"

# 清理旧的构建
echo "清理旧的构建..."
rm -rf "$APP_DIR"

# 创建目录结构
echo "创建目录结构..."
mkdir -p "$MACOS_DIR"
mkdir -p "$RESOURCES_DIR"
mkdir -p "$JAVA_DIR"

# 创建 Info.plist
echo "创建 Info.plist..."
cat > "$CONTENTS_DIR/Info.plist" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleExecutable</key>
    <string>openpnp</string>
    <key>CFBundleIconFile</key>
    <string>AppIcon</string>
    <key>CFBundleIdentifier</key>
    <string>org.openpnp</string>
    <key>CFBundleName</key>
    <string>OpenPnP</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>CFBundleShortVersionString</key>
    <string>1.0</string>
    <key>CFBundleVersion</key>
    <string>1</string>
    <key>NSHighResolutionCapable</key>
    <true/>
</dict>
</plist>
EOF

# 创建启动脚本
echo "创建启动脚本..."
cat > "$MACOS_DIR/openpnp" << EOF
#!/bin/bash
DIR="\$( cd "\$( dirname "\${BASH_SOURCE[0]}" )" && pwd )"
cd "\$DIR/../Java"
./bin/openpnp
EOF

# 设置启动脚本权限
echo "设置启动脚本权限..."
chmod +x "$MACOS_DIR/openpnp"

# 复制编译后的文件
echo "复制编译后的文件..."
if [ -d "$ASSEMBLER_DIR" ]; then
    cp -rv "$ASSEMBLER_DIR"/* "$JAVA_DIR/"
else
    echo "错误: 找不到编译后的文件"
    exit 1
fi

# 创建 DMG
echo "创建 DMG 文件..."
hdiutil create -volname "$APP_NAME" -srcfolder "$APP_DIR" -ov -format UDZO "$BUILD_DIR/$APP_NAME.dmg"

echo "完成！"
echo "应用程序包已创建: $APP_DIR"
echo "安装镜像已创建: $BUILD_DIR/$APP_NAME.dmg" 