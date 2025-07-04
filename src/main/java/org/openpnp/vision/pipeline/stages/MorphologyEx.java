package org.openpnp.vision.pipeline.stages;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.openpnp.vision.pipeline.CvPipeline;
import org.openpnp.vision.pipeline.CvStage;
import org.openpnp.vision.pipeline.Property;
import org.openpnp.vision.pipeline.Stage;
import org.simpleframework.xml.Attribute;

@Stage(category = "Image Processing", 
       description = "对图像进行形态学操作，包括膨胀、腐蚀、开运算、闭运算等。")
public class MorphologyEx extends CvStage {
    
    public enum MorphOp {
        ERODE("腐蚀", Imgproc.MORPH_ERODE),
        DILATE("膨胀", Imgproc.MORPH_DILATE),
        OPEN("开运算", Imgproc.MORPH_OPEN),
        CLOSE("闭运算", Imgproc.MORPH_CLOSE),
        GRADIENT("形态学梯度", Imgproc.MORPH_GRADIENT),
        TOPHAT("顶帽", Imgproc.MORPH_TOPHAT),
        BLACKHAT("黑帽", Imgproc.MORPH_BLACKHAT);
        
        private final String displayName;
        private final int opencvValue;
        
        MorphOp(String displayName, int opencvValue) {
            this.displayName = displayName;
            this.opencvValue = opencvValue;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getOpencvValue() {
            return opencvValue;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    public enum MorphShape {
        RECT("矩形", Imgproc.MORPH_RECT),
        ELLIPSE("椭圆形", Imgproc.MORPH_ELLIPSE),
        CROSS("十字形", Imgproc.MORPH_CROSS);
        
        private final String displayName;
        private final int opencvValue;
        
        MorphShape(String displayName, int opencvValue) {
            this.displayName = displayName;
            this.opencvValue = opencvValue;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getOpencvValue() {
            return opencvValue;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    @Attribute(required = false)
    @Property(description = "形态学操作类型")
    private MorphOp operation = MorphOp.DILATE;
    
    @Attribute(required = false)
    @Property(description = "结构元素形状")
    private MorphShape shape = MorphShape.RECT;
    
    @Attribute(required = false)
    @Property(description = "结构元素大小（像素）")
    private int kernelSize = 3;
    
    @Attribute(required = false)
    @Property(description = "迭代次数")
    private int iterations = 1;
    
    @Attribute(required = false)
    @Property(description = "是否先进行二值化处理")
    private boolean binarize = false;
    
    @Attribute(required = false)
    @Property(description = "二值化阈值（当启用二值化时使用）")
    private int threshold = 128;
    
    @Attribute(required = false)
    @Property(description = "是否反转二值化结果")
    private boolean invertThreshold = false;

    public MorphOp getOperation() {
        return operation;
    }

    public void setOperation(MorphOp operation) {
        this.operation = operation;
    }

    public MorphShape getShape() {
        return shape;
    }

    public void setShape(MorphShape shape) {
        this.shape = shape;
    }

    public int getKernelSize() {
        return kernelSize;
    }

    public void setKernelSize(int kernelSize) {
        this.kernelSize = Math.max(1, kernelSize);
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = Math.max(1, iterations);
    }

    public boolean isBinarize() {
        return binarize;
    }

    public void setBinarize(boolean binarize) {
        this.binarize = binarize;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = Math.max(0, Math.min(255, threshold));
    }

    public boolean isInvertThreshold() {
        return invertThreshold;
    }

    public void setInvertThreshold(boolean invertThreshold) {
        this.invertThreshold = invertThreshold;
    }

    @Override
    public Result process(CvPipeline pipeline) throws Exception {
        Mat mat = pipeline.getWorkingImage();
        
        // 如果需要二值化，先进行二值化处理
        if (binarize) {
            int type = invertThreshold ? Imgproc.THRESH_BINARY_INV : Imgproc.THRESH_BINARY;
            Imgproc.threshold(mat, mat, threshold, 255, type);
        }
        
        // 创建结构元素
        Mat kernel = Imgproc.getStructuringElement(shape.getOpencvValue(), 
                                                  new Size(kernelSize, kernelSize));
        
        // 执行形态学操作
        Imgproc.morphologyEx(mat, mat, operation.getOpencvValue(), kernel, 
                            new org.opencv.core.Point(-1, -1), iterations);
        
        // 释放结构元素
        kernel.release();
        
        return null;
    }
} 