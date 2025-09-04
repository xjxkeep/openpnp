package org.openpnp.vision.pipeline.stages;

import java.awt.Color;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openpnp.vision.FluentCv;
import org.openpnp.vision.pipeline.CvPipeline;
import org.openpnp.vision.pipeline.CvStage;
import org.simpleframework.xml.Attribute;

/**
 * Mask everything in the working image outside of a rectangle.
 * The rectangle is defined by x, y (top-left corner) and width, height.
 * If x and y are not specified, the rectangle will be centered.
 */
public class MaskRectangle extends CvStage {
    @Attribute(required = false)
    private int x = -1;
    
    @Attribute(required = false)
    private int y = -1;
    
    @Attribute(required = false)
    private int width = 100;
    
    @Attribute(required = false)
    private int height = 100;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public Result process(CvPipeline pipeline) throws Exception {
        Mat mat = pipeline.getWorkingImage();
        Mat mask = mat.clone();
        Mat masked = mat.clone();
        Scalar color = FluentCv.colorToScalar(Color.black);
        mask.setTo(color);
        masked.setTo(color);
        
        Point low, high;
        
        // 如果指定了 x 和 y，使用这些坐标作为左上角
        if (getX() >= 0 && getY() >= 0) {
            low = new Point(getX(), getY());
            high = new Point(getX() + getWidth(), getY() + getHeight());
        } else {
            // 否则使用默认的中心定位方式
            low = new Point(mat.cols() / 2 - getWidth() / 2, mat.rows() / 2 - getHeight() / 2);
            high = new Point(mat.cols() / 2 + getWidth() / 2, mat.rows() / 2 + getHeight() / 2);
        }
        
        Imgproc.rectangle(mask, low, high, new Scalar(255, 255, 255), -1);
        if (getWidth() * getHeight() < 0) {
            Core.bitwise_not(mask, mask);
        }
        mat.copyTo(masked, mask);
        mask.release();
        return new Result(masked);
    }
}
