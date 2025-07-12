# Nozzle Placement Offset Feature

## 概述

为OpenPnP的nozzle添加了`placeOffsetX`和`placeOffsetY`属性，用于在放置元件时校准静态偏差。

## 功能描述

### 新增属性
- `placeOffsetX`: X轴放置偏移量（毫米）
- `placeOffsetY`: Y轴放置偏移量（毫米）

### 工作原理
1. 在`moveToPlacementLocation`方法中，系统会检查这两个偏移值
2. 如果偏移值不为零，会在计算出的放置位置基础上添加这些偏移
3. 这样可以补偿nozzle的静态偏差，提高放置精度

### 界面配置
在Machine Setup面板中，选择任意nozzle，在"Coordinate System"标签页中可以看到：
- "Place Offset X" - 设置X轴偏移量
- "Place Offset Y" - 设置Y轴偏移量

### 支持的Nozzle类型
- `ReferenceNozzle` - 基础nozzle类型
- `ContactProbeNozzle` - 接触式探测nozzle（继承自ReferenceNozzle）

## 使用方法

1. 打开Machine Setup面板
2. 选择需要配置的nozzle
3. 在"Coordinate System"标签页中找到"Place Offset X"和"Place Offset Y"字段
4. 输入相应的偏移值（单位：毫米）
5. 点击"Apply"保存设置

## 技术实现

### 代码修改
1. **ReferenceNozzle.java**
   - 添加了`placeOffsetX`和`placeOffsetY`属性
   - 添加了getter/setter方法
   - 在`moveToPlacementLocation`方法中应用偏移

2. **ContactProbeNozzle.java**
   - 在`moveToPlacementLocation`方法中添加偏移支持

3. **ReferenceNozzleConfigurationWizard.java**
   - 添加了UI组件用于配置偏移值
   - 添加了数据绑定

4. **翻译文件**
   - 添加了英文和俄语翻译字符串

### 测试
创建了`ReferenceNozzlePlaceOffsetTest.java`测试文件，验证：
- 属性getter/setter功能
- 偏移值在放置位置计算中的正确应用
- 零偏移值时的正确行为

## 注意事项

- 偏移值以毫米为单位
- 正值表示向正方向偏移，负值表示向负方向偏移
- 这些偏移会在所有放置操作中生效
- 建议在使用前进行测试，确保偏移值设置正确 