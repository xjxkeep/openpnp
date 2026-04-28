/*
 * Copyright (C) 2026
 *
 * This file is part of OpenPnP.
 *
 * OpenPnP is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * OpenPnP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with OpenPnP. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * For more information about OpenPnP visit http://openpnp.org
 */

package org.openpnp.machine.reference.camera.calibration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.openpnp.model.AbstractModelObject;
import org.openpnp.model.Length;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.model.Point;
import org.pmw.tinylog.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

/**
 * Stores and evaluates a two-dimensional calibration grid captured with a down-looking camera.
 *
 * The nominal grid is generated from the captured outer corner-dot centers and the configured dot
 * pitch. Each measured grid point stores the detected machine coordinate and an optional local
 * pixel-to-machine 2x2 matrix. Inside the captured grid, corrections are interpolated linearly
 * over two triangles per cell; outside the grid callers keep their existing global behavior.
 */
public class ReferenceGridCalibration extends AbstractModelObject {
    private static final String LOG_PREFIX = "[CircleGridCalibration]";

    public static final int CORNER_LEFT_TOP = 0;
    public static final int CORNER_RIGHT_TOP = 1;
    public static final int CORNER_RIGHT_BOTTOM = 2;
    public static final int CORNER_LEFT_BOTTOM = 3;

    private static final double OUTSIDE_EPSILON = 1e-9;

    @Element(required = false)
    private Length circleDiameter = new Length(3, LengthUnit.Millimeters);

    @Element(required = false)
    private Length pitch = new Length(10, LengthUnit.Millimeters);

    @Element(required = false)
    private Length probeStep = new Length(1, LengthUnit.Millimeters);

    @Element(required = false)
    private Location leftTopCorner = new Location(LengthUnit.Millimeters);

    @Element(required = false)
    private Location rightTopCorner = new Location(LengthUnit.Millimeters);

    @Element(required = false)
    private Location rightBottomCorner = new Location(LengthUnit.Millimeters);

    @Element(required = false)
    private Location leftBottomCorner = new Location(LengthUnit.Millimeters);

    @Attribute(required = false)
    private boolean leftTopCornerSet;

    @Attribute(required = false)
    private boolean rightTopCornerSet;

    @Attribute(required = false)
    private boolean rightBottomCornerSet;

    @Attribute(required = false)
    private boolean leftBottomCornerSet;

    @Attribute(required = false)
    private int rows;

    @Attribute(required = false)
    private int columns;

    @Attribute(required = false)
    private double maxCornerResidualMm;

    @Attribute(required = false)
    private double rmsErrorMm;

    @Attribute(required = false)
    private double maxErrorMm;

    @Attribute(required = false)
    private boolean enabled;

    @ElementList(required = false)
    private List<GridPoint> gridPoints = new ArrayList<>();

    public Length getCircleDiameter() {
        return circleDiameter;
    }

    public void setCircleDiameter(Length circleDiameter) {
        Object oldValue = this.circleDiameter;
        this.circleDiameter = circleDiameter;
        firePropertyChange("circleDiameter", oldValue, circleDiameter);
    }

    public Length getPitch() {
        return pitch;
    }

    public void setPitch(Length pitch) {
        Object oldValue = this.pitch;
        this.pitch = pitch;
        firePropertyChange("pitch", oldValue, pitch);
    }

    public Length getProbeStep() {
        return probeStep;
    }

    public void setProbeStep(Length probeStep) {
        Object oldValue = this.probeStep;
        this.probeStep = probeStep;
        firePropertyChange("probeStep", oldValue, probeStep);
    }

    public Location getLeftTopCorner() {
        return leftTopCorner;
    }

    public Location getRightTopCorner() {
        return rightTopCorner;
    }

    public Location getRightBottomCorner() {
        return rightBottomCorner;
    }

    public Location getLeftBottomCorner() {
        return leftBottomCorner;
    }

    public Location getCorner(int index) {
        switch (index) {
            case CORNER_LEFT_TOP:
                return leftTopCorner;
            case CORNER_RIGHT_TOP:
                return rightTopCorner;
            case CORNER_RIGHT_BOTTOM:
                return rightBottomCorner;
            case CORNER_LEFT_BOTTOM:
                return leftBottomCorner;
            default:
                throw new IllegalArgumentException("Unknown corner index " + index);
        }
    }

    public void setCorner(int index, Location location) {
        switch (index) {
            case CORNER_LEFT_TOP:
                setLeftTopCorner(location);
                break;
            case CORNER_RIGHT_TOP:
                setRightTopCorner(location);
                break;
            case CORNER_RIGHT_BOTTOM:
                setRightBottomCorner(location);
                break;
            case CORNER_LEFT_BOTTOM:
                setLeftBottomCorner(location);
                break;
            default:
                throw new IllegalArgumentException("Unknown corner index " + index);
        }
    }

    public void setLeftTopCorner(Location leftTopCorner) {
        Object oldValue = this.leftTopCorner;
        this.leftTopCorner = leftTopCorner;
        this.leftTopCornerSet = leftTopCorner != null;
        firePropertyChange("leftTopCorner", oldValue, leftTopCorner);
    }

    public void setRightTopCorner(Location rightTopCorner) {
        Object oldValue = this.rightTopCorner;
        this.rightTopCorner = rightTopCorner;
        this.rightTopCornerSet = rightTopCorner != null;
        firePropertyChange("rightTopCorner", oldValue, rightTopCorner);
    }

    public void setRightBottomCorner(Location rightBottomCorner) {
        Object oldValue = this.rightBottomCorner;
        this.rightBottomCorner = rightBottomCorner;
        this.rightBottomCornerSet = rightBottomCorner != null;
        firePropertyChange("rightBottomCorner", oldValue, rightBottomCorner);
    }

    public void setLeftBottomCorner(Location leftBottomCorner) {
        Object oldValue = this.leftBottomCorner;
        this.leftBottomCorner = leftBottomCorner;
        this.leftBottomCornerSet = leftBottomCorner != null;
        firePropertyChange("leftBottomCorner", oldValue, leftBottomCorner);
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        Object oldValue = this.rows;
        this.rows = rows;
        firePropertyChange("rows", oldValue, rows);
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        Object oldValue = this.columns;
        this.columns = columns;
        firePropertyChange("columns", oldValue, columns);
    }

    public double getMaxCornerResidualMm() {
        return maxCornerResidualMm;
    }

    public void setMaxCornerResidualMm(double maxCornerResidualMm) {
        Object oldValue = this.maxCornerResidualMm;
        this.maxCornerResidualMm = maxCornerResidualMm;
        firePropertyChange("maxCornerResidualMm", oldValue, maxCornerResidualMm);
    }

    public double getRmsErrorMm() {
        return rmsErrorMm;
    }

    public void setRmsErrorMm(double rmsErrorMm) {
        Object oldValue = this.rmsErrorMm;
        this.rmsErrorMm = rmsErrorMm;
        firePropertyChange("rmsErrorMm", oldValue, rmsErrorMm);
    }

    public double getMaxErrorMm() {
        return maxErrorMm;
    }

    public void setMaxErrorMm(double maxErrorMm) {
        Object oldValue = this.maxErrorMm;
        this.maxErrorMm = maxErrorMm;
        firePropertyChange("maxErrorMm", oldValue, maxErrorMm);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        Object oldValue = this.enabled;
        this.enabled = enabled;
        firePropertyChange("enabled", oldValue, enabled);
        if (!oldValue.equals(enabled)) {
            Logger.info("{} enabled={} rows={} columns={} points={} rms={}mm max={}mm",
                    LOG_PREFIX, enabled, rows, columns, gridPoints.size(),
                    formatDouble(rmsErrorMm), formatDouble(maxErrorMm));
        }
    }

    public List<GridPoint> getGridPoints() {
        return gridPoints;
    }

    public void setGridPoints(List<GridPoint> gridPoints) {
        Object oldValue = this.gridPoints;
        this.gridPoints = gridPoints == null ? new ArrayList<>() : gridPoints;
        updateErrorStatistics();
        firePropertyChange("gridPoints", oldValue, this.gridPoints);
        Logger.info("{} loaded grid points count={} rms={}mm max={}mm",
                LOG_PREFIX, this.gridPoints.size(), formatDouble(rmsErrorMm), formatDouble(maxErrorMm));
    }

    public boolean areCornersComplete() {
        return isCornerSet(CORNER_LEFT_TOP)
                && isCornerSet(CORNER_RIGHT_TOP)
                && isCornerSet(CORNER_RIGHT_BOTTOM)
                && isCornerSet(CORNER_LEFT_BOTTOM);
    }

    public void clearCorners() {
        Logger.info("{} clearing captured corners and grid data", LOG_PREFIX);
        setEnabled(false);
        leftTopCorner = new Location(LengthUnit.Millimeters);
        rightTopCorner = new Location(LengthUnit.Millimeters);
        rightBottomCorner = new Location(LengthUnit.Millimeters);
        leftBottomCorner = new Location(LengthUnit.Millimeters);
        leftTopCornerSet = false;
        rightTopCornerSet = false;
        rightBottomCornerSet = false;
        leftBottomCornerSet = false;
        setRows(0);
        setColumns(0);
        setMaxCornerResidualMm(0);
        clearGridPoints();
        firePropertyChange("corners", null, null);
    }

    public int getNextCornerIndex() {
        for (int i = 0; i < 4; i++) {
            if (!isCornerSet(i)) {
                return i;
            }
        }
        return -1;
    }

    public boolean isCornerSet(int index) {
        switch (index) {
            case CORNER_LEFT_TOP:
                return leftTopCornerSet || leftTopCorner != null && leftTopCorner.isInitialized();
            case CORNER_RIGHT_TOP:
                return rightTopCornerSet || rightTopCorner != null && rightTopCorner.isInitialized();
            case CORNER_RIGHT_BOTTOM:
                return rightBottomCornerSet || rightBottomCorner != null && rightBottomCorner.isInitialized();
            case CORNER_LEFT_BOTTOM:
                return leftBottomCornerSet || leftBottomCorner != null && leftBottomCorner.isInitialized();
            default:
                return false;
        }
    }

    public String getCornerName(int index) {
        switch (index) {
            case CORNER_LEFT_TOP:
                return "左上";
            case CORNER_RIGHT_TOP:
                return "右上";
            case CORNER_RIGHT_BOTTOM:
                return "右下";
            case CORNER_LEFT_BOTTOM:
                return "左下";
            default:
                return "";
        }
    }

    public boolean inferGrid() throws Exception {
        if (!areCornersComplete()) {
            Logger.warn("{} cannot infer grid: corners are incomplete. ltSet={} rtSet={} rbSet={} lbSet={}",
                    LOG_PREFIX, isCornerSet(CORNER_LEFT_TOP), isCornerSet(CORNER_RIGHT_TOP),
                    isCornerSet(CORNER_RIGHT_BOTTOM), isCornerSet(CORNER_LEFT_BOTTOM));
            return false;
        }
        double pitchMm = getPitchMm();
        if (pitchMm <= 0) {
            Logger.warn("{} cannot infer grid: pitch={}mm", LOG_PREFIX, formatDouble(pitchMm));
            throw new Exception("Circle grid pitch must be greater than zero.");
        }

        Location lt = leftTopCorner.convertToUnits(LengthUnit.Millimeters);
        Location rt = rightTopCorner.convertToUnits(LengthUnit.Millimeters);
        Location rb = rightBottomCorner.convertToUnits(LengthUnit.Millimeters);
        Location lb = leftBottomCorner.convertToUnits(LengthUnit.Millimeters);

        double top = distance(lt, rt);
        double bottom = distance(lb, rb);
        double right = distance(rt, rb);
        double left = distance(lt, lb);

        int inferredColumns = (int) Math.round(((top + bottom) / 2.0) / pitchMm) + 1;
        int inferredRows = (int) Math.round(((left + right) / 2.0) / pitchMm) + 1;
        if (inferredColumns < 2 || inferredRows < 2) {
            Logger.warn("{} captured corners are too close. pitch={}mm top={}mm bottom={}mm left={}mm right={}mm",
                    LOG_PREFIX, formatDouble(pitchMm), formatDouble(top), formatDouble(bottom),
                    formatDouble(left), formatDouble(right));
            throw new Exception("The captured corners are too close together for the configured circle pitch.");
        }

        double expectedWidth = (inferredColumns - 1) * pitchMm;
        double expectedHeight = (inferredRows - 1) * pitchMm;
        double residual = Math.max(
                Math.max(Math.abs(top - expectedWidth), Math.abs(bottom - expectedWidth)),
                Math.max(Math.abs(left - expectedHeight), Math.abs(right - expectedHeight)));

        setColumns(inferredColumns);
        setRows(inferredRows);
        setMaxCornerResidualMm(residual);
        Logger.info("{} inferred grid rows={} columns={} pitch={}mm top={}mm bottom={}mm left={}mm right={}mm residual={}mm corners=[lt={}, rt={}, rb={}, lb={}]",
                LOG_PREFIX, inferredRows, inferredColumns, formatDouble(pitchMm),
                formatDouble(top), formatDouble(bottom), formatDouble(left), formatDouble(right),
                formatDouble(residual), formatLocation(lt), formatLocation(rt),
                formatLocation(rb), formatLocation(lb));
        return true;
    }

    public void clearGridPoints() {
        Object oldValue = gridPoints;
        int oldCount = gridPoints == null ? 0 : gridPoints.size();
        gridPoints = new ArrayList<>();
        setEnabled(false);
        setRmsErrorMm(0);
        setMaxErrorMm(0);
        firePropertyChange("gridPoints", oldValue, gridPoints);
        Logger.info("{} cleared grid points oldCount={}", LOG_PREFIX, oldCount);
    }

    public void setGridPoint(int row, int column, Location nominal, Location measured,
            PixelTransform pixelTransform) {
        GridPoint point = getGridPoint(row, column);
        if (point == null) {
            point = new GridPoint(row, column);
            gridPoints.add(point);
        }
        point.setNominal(nominal);
        point.setMeasured(measured);
        if (pixelTransform != null) {
            point.setPixelTransform(pixelTransform);
        }
        updateErrorStatistics();
        firePropertyChange("gridPoints", null, gridPoints);
        Logger.debug("{} stored grid point row={} column={} nominal={} measured={} correction={} pixelTransform={}",
                LOG_PREFIX, row, column, formatLocation(nominal), formatLocation(measured),
                formatLocation(point.getMotionCorrection()), formatPixelTransform(pixelTransform));
    }

    public GridPoint getGridPoint(int row, int column) {
        for (GridPoint point : gridPoints) {
            if (point.getRow() == row && point.getColumn() == column) {
                return point;
            }
        }
        return null;
    }

    public boolean isGridComplete() {
        if (rows < 2 || columns < 2) {
            return false;
        }
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                GridPoint point = getGridPoint(row, column);
                if (point == null || point.getNominal() == null || point.getMeasured() == null) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isMotionMappingValid() {
        return enabled && isGridComplete() && getBasis() != null;
    }

    public boolean isPixelMappingValid() {
        if (!isMotionMappingValid()) {
            return false;
        }
        for (GridPoint point : gridPoints) {
            if (!point.hasPixelTransform()) {
                return false;
            }
        }
        return true;
    }

    public Location getNominalLocation(int row, int column) {
        Basis basis = getBasis();
        if (basis == null) {
            return null;
        }
        double pitchMm = getPitchMm();
        double x = basis.origin.getX()
                + basis.xUnit.x * pitchMm * column
                + basis.yUnit.x * pitchMm * row;
        double y = basis.origin.getY()
                + basis.xUnit.y * pitchMm * column
                + basis.yUnit.y * pitchMm * row;
        return new Location(LengthUnit.Millimeters, x, y, basis.origin.getZ(), basis.origin.getRotation());
    }

    public Location getMotionCorrection(Location logicalLocation) {
        InterpolatedValue value = interpolate(logicalLocation);
        if (value == null) {
            return new Location(LengthUnit.Millimeters);
        }
        return new Location(LengthUnit.Millimeters, value.correctionX, value.correctionY, 0, 0);
    }

    public Location applyMotionMapping(Location logicalLocation) {
        Location correction = getMotionCorrection(logicalLocation).convertToUnits(logicalLocation.getUnits());
        return logicalLocation.add(correction);
    }

    public Location applyInverseMotionMapping(Location rawLocation) {
        Location logical = rawLocation.convertToUnits(LengthUnit.Millimeters);
        Location rawMm = rawLocation.convertToUnits(LengthUnit.Millimeters);
        for (int i = 0; i < 3; i++) {
            Location correction = getMotionCorrection(logical);
            logical = rawMm.subtract(correction);
        }
        return logical.convertToUnits(rawLocation.getUnits());
    }

    public Location getPixelOffsets(Location cameraLocation, Location fallbackUnitsPerPixel,
            double offsetX, double offsetY) {
        PixelTransform transform = getPixelTransform(cameraLocation, fallbackUnitsPerPixel);
        Location offset = transform.toLocation(offsetX, offsetY);
        return offset.convertToUnits(fallbackUnitsPerPixel.getUnits());
    }

    public Point getLocationPixels(Location cameraLocation, Location location,
            Location fallbackUnitsPerPixel) {
        PixelTransform transform = getPixelTransform(cameraLocation, fallbackUnitsPerPixel);
        Location delta = location.convertToUnits(LengthUnit.Millimeters)
                .subtract(cameraLocation.convertToUnits(LengthUnit.Millimeters));
        return transform.toPixels(delta.getX(), delta.getY());
    }

    public PixelTransform getPixelTransform(Location cameraLocation, Location fallbackUnitsPerPixel) {
        InterpolatedValue value = interpolate(cameraLocation);
        if (value == null || !value.pixelTransformValid) {
            return PixelTransform.fromUnitsPerPixel(fallbackUnitsPerPixel);
        }
        return new PixelTransform(value.pixelToMmXx, value.pixelToMmYx,
                value.pixelToMmXy, value.pixelToMmYy);
    }

    private InterpolatedValue interpolate(Location location) {
        if (!enabled || !isGridComplete()) {
            return null;
        }
        Basis basis = getBasis();
        if (basis == null) {
            return null;
        }
        GridCoordinate gridCoordinate = basis.toGrid(location.convertToUnits(LengthUnit.Millimeters));
        if (!gridCoordinate.isInside(rows, columns)) {
            return null;
        }

        int column0 = Math.min((int) Math.floor(gridCoordinate.column), columns - 2);
        int row0 = Math.min((int) Math.floor(gridCoordinate.row), rows - 2);
        double u = gridCoordinate.column - column0;
        double v = gridCoordinate.row - row0;
        u = clamp(u, 0, 1);
        v = clamp(v, 0, 1);

        GridPoint p00 = getGridPoint(row0, column0);
        GridPoint p10 = getGridPoint(row0, column0 + 1);
        GridPoint p01 = getGridPoint(row0 + 1, column0);
        GridPoint p11 = getGridPoint(row0 + 1, column0 + 1);
        if (p00 == null || p10 == null || p01 == null || p11 == null) {
            return null;
        }

        if (u + v <= 1.0) {
            return InterpolatedValue.interpolate(p00, p10, p01, 1.0 - u - v, u, v);
        }
        return InterpolatedValue.interpolate(p11, p01, p10, u + v - 1.0, 1.0 - u, 1.0 - v);
    }

    private void updateErrorStatistics() {
        double sumSquares = 0;
        double max = 0;
        int count = 0;
        for (GridPoint point : gridPoints) {
            if (point.getNominal() != null && point.getMeasured() != null) {
                Location correction = point.getMotionCorrection();
                double distance = Math.hypot(correction.getX(), correction.getY());
                sumSquares += distance * distance;
                max = Math.max(max, distance);
                count++;
            }
        }
        if (count == 0) {
            setRmsErrorMm(0);
            setMaxErrorMm(0);
        }
        else {
            setRmsErrorMm(Math.sqrt(sumSquares / count));
            setMaxErrorMm(max);
        }
    }

    private Basis getBasis() {
        if (!areCornersComplete() || rows < 2 || columns < 2 || getPitchMm() <= 0) {
            return null;
        }

        Location lt = leftTopCorner.convertToUnits(LengthUnit.Millimeters);
        Location rt = rightTopCorner.convertToUnits(LengthUnit.Millimeters);
        Location rb = rightBottomCorner.convertToUnits(LengthUnit.Millimeters);
        Location lb = leftBottomCorner.convertToUnits(LengthUnit.Millimeters);

        Vector top = Vector.between(lt, rt).unit();
        Vector bottom = Vector.between(lb, rb).unit();
        Vector left = Vector.between(lt, lb).unit();
        Vector right = Vector.between(rt, rb).unit();
        if (top == null || bottom == null || left == null || right == null) {
            return null;
        }

        Vector xUnit = top.add(bottom).unit();
        Vector yUnit = left.add(right).unit();
        if (xUnit == null || yUnit == null || Math.abs(xUnit.cross(yUnit)) < 1e-9) {
            return null;
        }
        return new Basis(lt, xUnit, yUnit, getPitchMm());
    }

    private double getPitchMm() {
        return pitch == null ? 0 : pitch.convertToUnits(LengthUnit.Millimeters).getValue();
    }

    private static double distance(Location a, Location b) {
        return a.getLinearDistanceTo(b);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String formatDouble(double value) {
        return String.format(Locale.US, "%.6f", value);
    }

    private static String formatLocation(Location location) {
        if (location == null) {
            return "null";
        }
        Location mm = location.convertToUnits(LengthUnit.Millimeters);
        return String.format(Locale.US, "(x=%.6f,y=%.6f,z=%.6f,r=%.6f)",
                mm.getX(), mm.getY(), mm.getZ(), mm.getRotation());
    }

    private static String formatPixelTransform(PixelTransform transform) {
        if (transform == null) {
            return "null";
        }
        return String.format(Locale.US, "[[%.9f,%.9f],[%.9f,%.9f]]",
                transform.getPixelToMmXx(), transform.getPixelToMmXy(),
                transform.getPixelToMmYx(), transform.getPixelToMmYy());
    }

    public static class GridPoint {
        @Attribute(required = false)
        private int row;

        @Attribute(required = false)
        private int column;

        @Element(required = false)
        private Location nominal;

        @Element(required = false)
        private Location measured;

        @Attribute(required = false)
        private boolean pixelTransformValid;

        @Attribute(required = false)
        private double pixelToMmXx;

        @Attribute(required = false)
        private double pixelToMmYx;

        @Attribute(required = false)
        private double pixelToMmXy;

        @Attribute(required = false)
        private double pixelToMmYy;

        public GridPoint() {
        }

        public GridPoint(int row, int column) {
            this.row = row;
            this.column = column;
        }

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public int getColumn() {
            return column;
        }

        public void setColumn(int column) {
            this.column = column;
        }

        public Location getNominal() {
            return nominal;
        }

        public void setNominal(Location nominal) {
            this.nominal = nominal;
        }

        public Location getMeasured() {
            return measured;
        }

        public void setMeasured(Location measured) {
            this.measured = measured;
        }

        public boolean hasPixelTransform() {
            return pixelTransformValid;
        }

        public PixelTransform getPixelTransform() {
            return new PixelTransform(pixelToMmXx, pixelToMmYx, pixelToMmXy, pixelToMmYy);
        }

        public void setPixelTransform(PixelTransform pixelTransform) {
            this.pixelTransformValid = pixelTransform != null;
            if (pixelTransform != null) {
                this.pixelToMmXx = pixelTransform.getPixelToMmXx();
                this.pixelToMmYx = pixelTransform.getPixelToMmYx();
                this.pixelToMmXy = pixelTransform.getPixelToMmXy();
                this.pixelToMmYy = pixelTransform.getPixelToMmYy();
            }
        }

        public Location getMotionCorrection() {
            if (nominal == null || measured == null) {
                return new Location(LengthUnit.Millimeters);
            }
            return measured.convertToUnits(LengthUnit.Millimeters)
                    .subtract(nominal.convertToUnits(LengthUnit.Millimeters));
        }
    }

    public static class PixelTransform {
        private final double pixelToMmXx;
        private final double pixelToMmYx;
        private final double pixelToMmXy;
        private final double pixelToMmYy;

        public PixelTransform(double pixelToMmXx, double pixelToMmYx,
                double pixelToMmXy, double pixelToMmYy) {
            this.pixelToMmXx = pixelToMmXx;
            this.pixelToMmYx = pixelToMmYx;
            this.pixelToMmXy = pixelToMmXy;
            this.pixelToMmYy = pixelToMmYy;
        }

        public static PixelTransform fromUnitsPerPixel(Location unitsPerPixel) {
            Location upp = unitsPerPixel.convertToUnits(LengthUnit.Millimeters);
            return new PixelTransform(upp.getX(), 0, 0, -upp.getY());
        }

        public double getPixelToMmXx() {
            return pixelToMmXx;
        }

        public double getPixelToMmYx() {
            return pixelToMmYx;
        }

        public double getPixelToMmXy() {
            return pixelToMmXy;
        }

        public double getPixelToMmYy() {
            return pixelToMmYy;
        }

        public Location toLocation(double pixelX, double pixelY) {
            double x = pixelX * pixelToMmXx + pixelY * pixelToMmXy;
            double y = pixelX * pixelToMmYx + pixelY * pixelToMmYy;
            return new Location(LengthUnit.Millimeters, x, y, 0, 0);
        }

        public Point toPixels(double mmX, double mmY) {
            double determinant = pixelToMmXx * pixelToMmYy - pixelToMmXy * pixelToMmYx;
            if (Math.abs(determinant) < 1e-12) {
                return new Point(0, 0);
            }
            double pixelX = (mmX * pixelToMmYy - pixelToMmXy * mmY) / determinant;
            double pixelY = (pixelToMmXx * mmY - mmX * pixelToMmYx) / determinant;
            return new Point(pixelX, pixelY);
        }
    }

    private static class InterpolatedValue {
        private double correctionX;
        private double correctionY;
        private boolean pixelTransformValid;
        private double pixelToMmXx;
        private double pixelToMmYx;
        private double pixelToMmXy;
        private double pixelToMmYy;

        private static InterpolatedValue interpolate(GridPoint a, GridPoint b, GridPoint c,
                double wa, double wb, double wc) {
            InterpolatedValue value = new InterpolatedValue();
            Location ca = a.getMotionCorrection();
            Location cb = b.getMotionCorrection();
            Location cc = c.getMotionCorrection();
            value.correctionX = wa * ca.getX() + wb * cb.getX() + wc * cc.getX();
            value.correctionY = wa * ca.getY() + wb * cb.getY() + wc * cc.getY();

            value.pixelTransformValid = a.hasPixelTransform() && b.hasPixelTransform() && c.hasPixelTransform();
            if (value.pixelTransformValid) {
                PixelTransform pa = a.getPixelTransform();
                PixelTransform pb = b.getPixelTransform();
                PixelTransform pc = c.getPixelTransform();
                value.pixelToMmXx = wa * pa.pixelToMmXx + wb * pb.pixelToMmXx + wc * pc.pixelToMmXx;
                value.pixelToMmYx = wa * pa.pixelToMmYx + wb * pb.pixelToMmYx + wc * pc.pixelToMmYx;
                value.pixelToMmXy = wa * pa.pixelToMmXy + wb * pb.pixelToMmXy + wc * pc.pixelToMmXy;
                value.pixelToMmYy = wa * pa.pixelToMmYy + wb * pb.pixelToMmYy + wc * pc.pixelToMmYy;
            }
            return value;
        }
    }

    private static class Basis {
        private final Location origin;
        private final Vector xUnit;
        private final Vector yUnit;
        private final double pitchMm;

        private Basis(Location origin, Vector xUnit, Vector yUnit, double pitchMm) {
            this.origin = origin;
            this.xUnit = xUnit;
            this.yUnit = yUnit;
            this.pitchMm = pitchMm;
        }

        private GridCoordinate toGrid(Location location) {
            double dx = location.getX() - origin.getX();
            double dy = location.getY() - origin.getY();
            double determinant = xUnit.cross(yUnit);
            double column = (dx * yUnit.y - dy * yUnit.x) / determinant / pitchMm;
            double row = (xUnit.x * dy - xUnit.y * dx) / determinant / pitchMm;
            return new GridCoordinate(row, column);
        }
    }

    private static class GridCoordinate {
        private final double row;
        private final double column;

        private GridCoordinate(double row, double column) {
            this.row = row;
            this.column = column;
        }

        private boolean isInside(int rows, int columns) {
            return row >= -OUTSIDE_EPSILON && row <= rows - 1 + OUTSIDE_EPSILON
                    && column >= -OUTSIDE_EPSILON && column <= columns - 1 + OUTSIDE_EPSILON;
        }
    }

    private static class Vector {
        private final double x;
        private final double y;

        private Vector(double x, double y) {
            this.x = x;
            this.y = y;
        }

        private static Vector between(Location a, Location b) {
            return new Vector(b.getX() - a.getX(), b.getY() - a.getY());
        }

        private Vector add(Vector other) {
            return new Vector(x + other.x, y + other.y);
        }

        private Vector unit() {
            double length = Math.hypot(x, y);
            if (length == 0) {
                return null;
            }
            return new Vector(x / length, y / length);
        }

        private double cross(Vector other) {
            return x * other.y - y * other.x;
        }
    }
}
