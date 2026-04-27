/*
 * Copyright (C) 2026
 *
 * This file is part of OpenPnP.
 */

package org.openpnp.model;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * A 3x3 projective transform for coordinates on a planar board.
 *
 * The matrix maps local homogeneous coordinates to parent/global homogeneous
 * coordinates:
 *
 * <pre>
 * [x']   [m00 m01 m02] [x]
 * [y'] = [m10 m11 m12] [y]
 * [w']   [m20 m21 m22] [1]
 * </pre>
 */
@Root
public class HomographyTransform {
    @Attribute
    private double m00 = 1.0;
    @Attribute
    private double m01 = 0.0;
    @Attribute
    private double m02 = 0.0;
    @Attribute
    private double m10 = 0.0;
    @Attribute
    private double m11 = 1.0;
    @Attribute
    private double m12 = 0.0;
    @Attribute
    private double m20 = 0.0;
    @Attribute
    private double m21 = 0.0;
    @Attribute
    private double m22 = 1.0;

    public HomographyTransform() {
    }

    public HomographyTransform(double m00, double m01, double m02,
            double m10, double m11, double m12,
            double m20, double m21, double m22) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        normalize();
    }

    public HomographyTransform(HomographyTransform other) {
        this(other.m00, other.m01, other.m02,
                other.m10, other.m11, other.m12,
                other.m20, other.m21, other.m22);
    }

    public static HomographyTransform fromAffine(AffineTransform tx) {
        double[] m = new double[6];
        tx.getMatrix(m); // m00 m10 m01 m11 m02 m12
        return new HomographyTransform(
                m[0], m[2], m[4],
                m[1], m[3], m[5],
                0.0, 0.0, 1.0);
    }

    public static HomographyTransform scale(double sx, double sy) {
        return new HomographyTransform(sx, 0.0, 0.0, 0.0, sy, 0.0, 0.0, 0.0, 1.0);
    }

    public static HomographyTransform derive(List<Location> source, List<Location> destination) {
        int n = Math.min(source.size(), destination.size());
        if (n < 4) {
            throw new IllegalArgumentException("At least four point pairs are required for homography.");
        }

        RealMatrix a = MatrixUtils.createRealMatrix(2 * n, 8);
        RealMatrix b = MatrixUtils.createColumnRealMatrix(new double[2 * n]);
        for (int i = 0; i < n; i++) {
            Location s = source.get(i).convertToUnits(LengthUnit.Millimeters);
            Location d = destination.get(i).convertToUnits(LengthUnit.Millimeters);
            double x = s.getX();
            double y = s.getY();
            double u = d.getX();
            double v = d.getY();

            a.setRow(2 * i, new double[] { x, y, 1.0, 0.0, 0.0, 0.0, -u * x, -u * y });
            b.setEntry(2 * i, 0, u);
            a.setRow(2 * i + 1, new double[] { 0.0, 0.0, 0.0, x, y, 1.0, -v * x, -v * y });
            b.setEntry(2 * i + 1, 0, v);
        }

        RealMatrix h = new SingularValueDecomposition(a).getSolver().solve(b);
        return new HomographyTransform(
                h.getEntry(0, 0), h.getEntry(1, 0), h.getEntry(2, 0),
                h.getEntry(3, 0), h.getEntry(4, 0), h.getEntry(5, 0),
                h.getEntry(6, 0), h.getEntry(7, 0), 1.0);
    }

    public Point2D.Double transform(double x, double y) {
        double w = m20 * x + m21 * y + m22;
        return new Point2D.Double(
                (m00 * x + m01 * y + m02) / w,
                (m10 * x + m11 * y + m12) / w);
    }

    public HomographyTransform concatenate(HomographyTransform after) {
        RealMatrix a = toMatrix();
        RealMatrix b = after.toMatrix();
        return fromMatrix(a.multiply(b));
    }

    public HomographyTransform createInverse() {
        return fromMatrix(new SingularValueDecomposition(toMatrix()).getSolver().getInverse());
    }

    public double getRotationAngleAt(double x, double y) {
        Point2D.Double p0 = transform(x, y);
        Point2D.Double p1 = transform(x + 1.0, y);
        return Math.toDegrees(Math.atan2(p1.y - p0.y, p1.x - p0.x));
    }

    public AffineTransform toAffineApproximation() {
        Point2D.Double p0 = transform(0.0, 0.0);
        Point2D.Double px = transform(1.0, 0.0);
        Point2D.Double py = transform(0.0, 1.0);
        return new AffineTransform(
                px.x - p0.x, px.y - p0.y,
                py.x - p0.x, py.y - p0.y,
                p0.x, p0.y);
    }

    private RealMatrix toMatrix() {
        return MatrixUtils.createRealMatrix(new double[][] {
            { m00, m01, m02 },
            { m10, m11, m12 },
            { m20, m21, m22 }
        });
    }

    private static HomographyTransform fromMatrix(RealMatrix matrix) {
        return new HomographyTransform(
                matrix.getEntry(0, 0), matrix.getEntry(0, 1), matrix.getEntry(0, 2),
                matrix.getEntry(1, 0), matrix.getEntry(1, 1), matrix.getEntry(1, 2),
                matrix.getEntry(2, 0), matrix.getEntry(2, 1), matrix.getEntry(2, 2));
    }

    private void normalize() {
        if (m22 != 0.0 && m22 != 1.0) {
            m00 /= m22;
            m01 /= m22;
            m02 /= m22;
            m10 /= m22;
            m11 /= m22;
            m12 /= m22;
            m20 /= m22;
            m21 /= m22;
            m22 = 1.0;
        }
    }

    @Override
    public String toString() {
        return String.format(
                "[[%f, %f, %f], [%f, %f, %f], [%f, %f, %f]]",
                m00, m01, m02, m10, m11, m12, m20, m21, m22);
    }
}
