package org.openpnp.vision.pipeline.stages;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.openpnp.vision.pipeline.CvPipeline;
import org.openpnp.vision.pipeline.CvStage;

public class MorphologyExTest {
    
    @Test
    public void testMorphologyExCreation() {
        MorphologyEx stage = new MorphologyEx();
        assertNotNull(stage);
        assertEquals(MorphologyEx.MorphOp.DILATE, stage.getOperation());
        assertEquals(MorphologyEx.MorphShape.RECT, stage.getShape());
        assertEquals(3, stage.getKernelSize());
        assertEquals(1, stage.getIterations());
        assertFalse(stage.isBinarize());
        assertEquals(128, stage.getThreshold());
        assertFalse(stage.isInvertThreshold());
    }
    
    @Test
    public void testMorphologyExProperties() {
        MorphologyEx stage = new MorphologyEx();
        
        // 测试设置属性
        stage.setOperation(MorphologyEx.MorphOp.ERODE);
        stage.setShape(MorphologyEx.MorphShape.ELLIPSE);
        stage.setKernelSize(5);
        stage.setIterations(2);
        stage.setBinarize(true);
        stage.setThreshold(100);
        stage.setInvertThreshold(true);
        
        // 验证属性设置
        assertEquals(MorphologyEx.MorphOp.ERODE, stage.getOperation());
        assertEquals(MorphologyEx.MorphShape.ELLIPSE, stage.getShape());
        assertEquals(5, stage.getKernelSize());
        assertEquals(2, stage.getIterations());
        assertTrue(stage.isBinarize());
        assertEquals(100, stage.getThreshold());
        assertTrue(stage.isInvertThreshold());
    }
    
    @Test
    public void testMorphologyExValidation() {
        MorphologyEx stage = new MorphologyEx();
        
        // 测试kernelSize验证
        stage.setKernelSize(0);
        assertEquals(1, stage.getKernelSize()); // 应该被设置为最小值1
        
        stage.setKernelSize(-5);
        assertEquals(1, stage.getKernelSize()); // 应该被设置为最小值1
        
        // 测试iterations验证
        stage.setIterations(0);
        assertEquals(1, stage.getIterations()); // 应该被设置为最小值1
        
        stage.setIterations(-3);
        assertEquals(1, stage.getIterations()); // 应该被设置为最小值1
        
        // 测试threshold验证
        stage.setThreshold(-10);
        assertEquals(0, stage.getThreshold()); // 应该被限制为0
        
        stage.setThreshold(300);
        assertEquals(255, stage.getThreshold()); // 应该被限制为255
    }
    
    @Test
    public void testMorphologyExEnums() {
        // 测试MorphOp枚举
        assertEquals("腐蚀", MorphologyEx.MorphOp.ERODE.getDisplayName());
        assertEquals("膨胀", MorphologyEx.MorphOp.DILATE.getDisplayName());
        assertEquals("开运算", MorphologyEx.MorphOp.OPEN.getDisplayName());
        assertEquals("闭运算", MorphologyEx.MorphOp.CLOSE.getDisplayName());
        assertEquals("形态学梯度", MorphologyEx.MorphOp.GRADIENT.getDisplayName());
        assertEquals("顶帽", MorphologyEx.MorphOp.TOPHAT.getDisplayName());
        assertEquals("黑帽", MorphologyEx.MorphOp.BLACKHAT.getDisplayName());
        
        // 测试MorphShape枚举
        assertEquals("矩形", MorphologyEx.MorphShape.RECT.getDisplayName());
        assertEquals("椭圆形", MorphologyEx.MorphShape.ELLIPSE.getDisplayName());
        assertEquals("十字形", MorphologyEx.MorphShape.CROSS.getDisplayName());
    }
    
    @Test
    public void testMorphologyExStageRegistration() {
        // 测试Stage注解
        MorphologyEx stage = new MorphologyEx();
        assertEquals("Image Processing", stage.getCategory());
        assertEquals("对图像进行形态学操作，包括膨胀、腐蚀、开运算、闭运算等。", stage.getDescription());
    }
} 