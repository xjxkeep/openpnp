#!/bin/bash

platform='unknown'
unamestr=`uname`
case "$unamestr" in
	Linux)
		platform='linux'
		rootdir="$(dirname $(readlink -f $0))"
	;;
	Darwin)
		platform='mac'
		rootdir="$(cd $(dirname $0); pwd -P)"
	;;
esac

# 检查源文件是否有变更
check_source_changes() {
    # 获取所有 Java 源文件的最后修改时间
    latest_change=$(find src -name "*.java" -type f -exec stat -f "%m" {} \; | sort -n | tail -1)
    
    # 获取 target 目录下编译后的文件时间
    if [ -f "$rootdir/target/openpnp-gui-0.0.1-alpha-SNAPSHOT.jar" ]; then
        jar_time=$(stat -f "%m" "$rootdir/target/openpnp-gui-0.0.1-alpha-SNAPSHOT.jar")
    else
        jar_time=0
    fi
    
    # 如果源文件比编译文件新，则需要重新编译
    if [ "$latest_change" -gt "$jar_time" ]; then
        echo "检测到源文件变更，正在重新编译..."
        mvn package -DskipTests
    fi
}

# 在启动前检查是否需要重新编译
check_source_changes

case "$platform" in
	mac)
		java -Xdock:name=OpenPnP --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.desktop/java.awt=ALL-UNNAMED --add-opens=java.desktop/java.awt.color=ALL-UNNAMED --add-opens=java.desktop/com.apple.eawt=ALL-UNNAMED -jar $rootdir/target/openpnp-gui-0.0.1-alpha-SNAPSHOT.jar
	;;
	linux)
		java $1 --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.desktop/java.awt=ALL-UNNAMED --add-opens=java.desktop/java.awt.color=ALL-UNNAMED -jar $rootdir/target/openpnp-gui-0.0.1-alpha-SNAPSHOT.jar
	;;
esac
