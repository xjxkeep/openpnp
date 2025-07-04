# MorphologyEx CVStage 使用说明

## 概述

`MorphologyEx` 是一个新的OpenPnP CVStage，用于对图像进行形态学操作，包括膨胀、腐蚀、开运算、闭运算等。这个CVStage基于OpenCV的`morphologyEx`函数实现。

## 功能特性

### 支持的形态学操作
- **腐蚀 (Erode)**: 缩小图像中的白色区域
- **膨胀 (Dilate)**: 扩大图像中的白色区域  
- **开运算 (Open)**: 先腐蚀后膨胀，用于去除小噪点
- **闭运算 (Close)**: 先膨胀后腐蚀，用于填充小孔洞
- **形态学梯度 (Gradient)**: 膨胀减去腐蚀，用于边缘检测
- **顶帽 (Top Hat)**: 原图减去开运算结果
- **黑帽 (Black Hat)**: 闭运算结果减去原图

### 结构元素形状
- **矩形 (Rect)**: 矩形结构元素
- **椭圆形 (Ellipse)**: 椭圆形结构元素
- **十字形 (Cross)**: 十字形结构元素

## 参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| operation | MorphOp | DILATE | 形态学操作类型 |
| shape | MorphShape | RECT | 结构元素形状 |
| kernelSize | int | 3 | 结构元素大小（像素） |
| iterations | int | 1 | 迭代次数 |
| binarize | boolean | false | 是否先进行二值化处理 |
| threshold | int | 128 | 二值化阈值（0-255） |
| invertThreshold | boolean | false | 是否反转二值化结果 |

## 使用示例

### 基本膨胀操作
```xml
<stage name="Dilate" class="org.openpnp.vision.pipeline.stages.MorphologyEx">
    <property name="operation" value="DILATE"/>
    <property name="shape" value="RECT"/>
    <property name="kernelSize" value="3"/>
    <property name="iterations" value="1"/>
</stage>
```

### 腐蚀操作
```xml
<stage name="Erode" class="org.openpnp.vision.pipeline.stages.MorphologyEx">
    <property name="operation" value="ERODE"/>
    <property name="shape" value="ELLIPSE"/>
    <property name="kernelSize" value="5"/>
    <property name="iterations" value="2"/>
</stage>
```

### 带二值化的开运算
```xml
<stage name="OpenWithBinarize" class="org.openpnp.vision.pipeline.stages.MorphologyEx">
    <property name="operation" value="OPEN"/>
    <property name="shape" value="RECT"/>
    <property name="kernelSize" value="3"/>
    <property name="iterations" value="1"/>
    <property name="binarize" value="true"/>
    <property name="threshold" value="128"/>
    <property name="invertThreshold" value="false"/>
</stage>
```

## 在OpenPnP中使用

1. 在OpenPnP的视觉管道编辑器中，点击"添加阶段"
2. 选择"MorphologyEx"阶段
3. 配置所需的参数
4. 运行管道查看效果

## 应用场景

### PCB检测
- **去除噪点**: 使用开运算去除小的噪点
- **填充孔洞**: 使用闭运算填充小的孔洞
- **边缘增强**: 使用形态学梯度增强边缘

### 零件检测
- **形状分析**: 使用膨胀和腐蚀分析零件形状
- **缺陷检测**: 使用顶帽和黑帽检测缺陷

### 图像预处理
- **二值化后处理**: 对二值化图像进行形态学操作
- **噪声去除**: 使用开运算去除噪声

## 注意事项

1. **性能考虑**: 较大的kernelSize和iterations会增加处理时间
2. **内存使用**: 形态学操作会创建临时Mat对象，注意内存管理
3. **参数调优**: 根据具体应用场景调整参数以获得最佳效果
4. **二值化**: 如果输入图像不是二值图像，建议启用二值化选项

## 技术实现

该CVStage基于OpenCV的`morphologyEx`函数实现，支持所有标准的形态学操作。结构元素通过`getStructuringElement`函数创建，支持矩形、椭圆形和十字形三种形状。

代码中包含了完整的参数验证，确保输入参数在合理范围内，并提供了中文界面支持。 