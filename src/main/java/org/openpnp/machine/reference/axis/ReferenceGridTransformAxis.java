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

package org.openpnp.machine.reference.axis;

import java.util.Arrays;
import java.util.Locale;

import org.openpnp.ConfigurationListener;
import org.openpnp.gui.support.Wizard;
import org.openpnp.machine.reference.axis.wizards.ReferenceGridTransformAxisConfigurationWizard;
import org.openpnp.machine.reference.camera.ReferenceCamera;
import org.openpnp.machine.reference.camera.calibration.ReferenceGridCalibration;
import org.openpnp.model.AxesLocation;
import org.openpnp.model.Configuration;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.spi.Axis;
import org.openpnp.spi.Camera;
import org.openpnp.spi.Machine;
import org.openpnp.spi.Locatable.LocationOption;
import org.openpnp.spi.base.AbstractAxis;
import org.openpnp.spi.base.AbstractMachine;
import org.openpnp.spi.base.AbstractTransformedAxis;
import org.pmw.tinylog.Logger;
import org.simpleframework.xml.Attribute;

/**
 * A paired X/Y transformed axis that applies the grid correction captured by a
 * {@link ReferenceGridCalibration}. The two axis instances are grouped by their calibration camera
 * and input X/Y axes.
 */
public class ReferenceGridTransformAxis extends AbstractTransformedAxis {
    private static final String LOG_PREFIX = "[CircleGridAxis]";

    private AbstractAxis inputAxisX;
    private AbstractAxis inputAxisY;
    private ReferenceCamera calibrationCamera;

    @Attribute(required = false)
    private String inputAxisXId;

    @Attribute(required = false)
    private String inputAxisYId;

    @Attribute(required = false)
    private String calibrationCameraId;

    @Attribute(required = false)
    private boolean compensation = true;

    public ReferenceGridTransformAxis() {
        Configuration.get().addListener(new ConfigurationListener.Adapter() {
            @Override
            public void configurationLoaded(Configuration configuration) throws Exception {
                inputAxisX = (AbstractAxis) configuration.getMachine().getAxis(inputAxisXId);
                inputAxisY = (AbstractAxis) configuration.getMachine().getAxis(inputAxisYId);
                calibrationCamera = findCamera(configuration.getMachine(), calibrationCameraId);
                if (calibrationCameraId != null && calibrationCamera == null) {
                    Logger.warn("{} axis={} could not resolve calibration camera id={}",
                            LOG_PREFIX, getName(), calibrationCameraId);
                }
                Logger.info("{} loaded axis={} type={} inputX={} inputY={} camera={} compensation={}",
                        LOG_PREFIX, getName(), getType(), getAxisName(inputAxisX), getAxisName(inputAxisY),
                        calibrationCamera == null ? "null" : calibrationCamera.getName(), compensation);
            }
        });
    }

    public AbstractAxis getPrimaryInputAxis() {
        switch (type) {
            case X:
                return inputAxisX;
            case Y:
                return inputAxisY;
            default:
                return null;
        }
    }

    @Override
    public AxesLocation getCoordinateAxes(Machine machine) {
        return new AxesLocation((a, b) -> (a),
                AbstractAxis.getCoordinateAxes(inputAxisX, machine),
                AbstractAxis.getCoordinateAxes(inputAxisY, machine));
    }

    public AbstractAxis getInputAxisX() {
        return inputAxisX;
    }

    public void setInputAxisX(AbstractAxis inputAxisX) {
        Object oldValue = this.inputAxisX;
        this.inputAxisX = inputAxisX;
        this.inputAxisXId = inputAxisX == null ? null : inputAxisX.getId();
        firePropertyChange("inputAxisX", oldValue, inputAxisX);
    }

    public AbstractAxis getInputAxisY() {
        return inputAxisY;
    }

    public void setInputAxisY(AbstractAxis inputAxisY) {
        Object oldValue = this.inputAxisY;
        this.inputAxisY = inputAxisY;
        this.inputAxisYId = inputAxisY == null ? null : inputAxisY.getId();
        firePropertyChange("inputAxisY", oldValue, inputAxisY);
    }

    public ReferenceCamera getCalibrationCamera() {
        return calibrationCamera;
    }

    public void setCalibrationCamera(ReferenceCamera calibrationCamera) {
        Object oldValue = this.calibrationCamera;
        this.calibrationCamera = calibrationCamera;
        this.calibrationCameraId = calibrationCamera == null ? null : calibrationCamera.getId();
        firePropertyChange("calibrationCamera", oldValue, calibrationCamera);
    }

    public boolean isCompensation() {
        return compensation;
    }

    public void setCompensation(boolean compensation) {
        Object oldValue = this.compensation;
        this.compensation = compensation;
        firePropertyChange("compensation", oldValue, compensation);
    }

    public ReferenceGridCalibration getCalibration() {
        return calibrationCamera == null ? null : calibrationCamera.getGridCalibration();
    }

    @Override
    public AxesLocation toTransformed(AxesLocation location, LocationOption... options) {
        ReferenceGridTransformAxis[] axes = getGridAxes(location);
        ReferenceGridTransformAxis xAxis = axes[Axis.Type.X.ordinal()];
        ReferenceGridTransformAxis yAxis = axes[Axis.Type.Y.ordinal()];
        if (xAxis != null && yAxis != null && location.contains(xAxis) && location.contains(yAxis)) {
            return location;
        }

        location = AbstractTransformedAxis.toTransformed(inputAxisX, location, options);
        location = AbstractTransformedAxis.toTransformed(inputAxisY, location, options);

        if (inputAxisX == null || inputAxisY == null) {
            return location.put(new AxesLocation(this, 0.0));
        }

        double rawX = location.getCoordinate(inputAxisX);
        double rawY = location.getCoordinate(inputAxisY);
        Location transformed = new Location(LengthUnit.Millimeters, rawX, rawY, 0, 0);
        ReferenceGridCalibration calibration = getCalibration();
        if (shouldApply(calibration, options)) {
            Location before = transformed;
            transformed = calibration.applyInverseMotionMapping(transformed);
            Logger.debug("{} toTransformed camera={} raw={} transformed={} correction={}",
                    LOG_PREFIX, getCameraName(), formatLocation(before), formatLocation(transformed),
                    formatLocation(transformed.subtract(before)));
        }

        if (xAxis != null) {
            location = location.put(new AxesLocation(xAxis, transformed.getX()));
        }
        if (yAxis != null) {
            location = location.put(new AxesLocation(yAxis, transformed.getY()));
        }
        if (xAxis == null && yAxis == null) {
            location = location.put(new AxesLocation(this,
                    getType() == Axis.Type.Y ? transformed.getY() : transformed.getX()));
        }
        return location;
    }

    @Override
    public AxesLocation toRaw(AxesLocation location, LocationOption... options) throws Exception {
        if (inputAxisX == null || inputAxisY == null) {
            Logger.error("{} axis={} missing input axes inputX={} inputY={}",
                    LOG_PREFIX, getName(), getAxisName(inputAxisX), getAxisName(inputAxisY));
            throw new Exception(getName() + " must have both input X and Y axes set.");
        }
        if (location.contains(inputAxisX) && location.contains(inputAxisY)) {
            return location;
        }

        ReferenceGridTransformAxis[] axes = getGridAxes(location);
        ReferenceGridTransformAxis xAxis = axes[Axis.Type.X.ordinal()];
        ReferenceGridTransformAxis yAxis = axes[Axis.Type.Y.ordinal()];

        double transformedX = getCoordinate(location, xAxis, inputAxisX, Axis.Type.X);
        double transformedY = getCoordinate(location, yAxis, inputAxisY, Axis.Type.Y);
        Location raw = new Location(LengthUnit.Millimeters, transformedX, transformedY, 0, 0);

        ReferenceGridCalibration calibration = getCalibration();
        if (shouldApply(calibration, options)) {
            Location before = raw;
            raw = calibration.applyMotionMapping(raw);
            Logger.debug("{} toRaw camera={} logical={} raw={} correction={}",
                    LOG_PREFIX, getCameraName(), formatLocation(before), formatLocation(raw),
                    formatLocation(raw.subtract(before)));
        }

        location = location.put(new AxesLocation(inputAxisX, raw.getX()));
        location = location.put(new AxesLocation(inputAxisY, raw.getY()));
        location = AbstractTransformedAxis.toRaw(inputAxisX, location, options);
        location = AbstractTransformedAxis.toRaw(inputAxisY, location, options);
        return location;
    }

    private double getCoordinate(AxesLocation location, ReferenceGridTransformAxis axis,
            AbstractAxis inputAxis, Axis.Type axisType) {
        if (axis != null) {
            return location.getCoordinate(axis);
        }
        if (getType() == axisType) {
            return location.getCoordinate(this);
        }
        return location.getCoordinate(inputAxis);
    }

    private boolean shouldApply(ReferenceGridCalibration calibration, LocationOption... options) {
        return calibration != null
                && calibration.isMotionMappingValid()
                && (compensation == false
                        || !Arrays.asList(options).contains(LocationOption.SuppressStaticCompensation));
    }

    private ReferenceGridTransformAxis[] getGridAxes(AxesLocation location) {
        ReferenceGridTransformAxis[] axes = new ReferenceGridTransformAxis[Axis.Type.values().length];
        for (Axis axis : location.getAxes()) {
            if (axis instanceof ReferenceGridTransformAxis && isSameGridGroup((ReferenceGridTransformAxis) axis)) {
                axes[axis.getType().ordinal()] = (ReferenceGridTransformAxis) axis;
            }
        }
        if (axes[Axis.Type.X.ordinal()] == null || axes[Axis.Type.Y.ordinal()] == null) {
            for (Axis axis : Configuration.get().getMachine().getAxes()) {
                if (axis instanceof ReferenceGridTransformAxis && isSameGridGroup((ReferenceGridTransformAxis) axis)) {
                    axes[axis.getType().ordinal()] = (ReferenceGridTransformAxis) axis;
                }
            }
        }
        axes[getType().ordinal()] = this;
        return axes;
    }

    private boolean isSameGridGroup(ReferenceGridTransformAxis other) {
        if (other == null) {
            return false;
        }
        if (calibrationCamera != null && other.calibrationCamera != null) {
            return calibrationCamera == other.calibrationCamera;
        }
        if (calibrationCameraId != null && calibrationCameraId.equals(other.calibrationCameraId)) {
            return true;
        }
        return inputAxisX != null && inputAxisX == other.inputAxisX
                && inputAxisY != null && inputAxisY == other.inputAxisY;
    }

    private static ReferenceCamera findCamera(Machine machine, String cameraId) {
        if (cameraId == null) {
            return null;
        }
        for (Camera camera : machine.getAllCameras()) {
            if (camera instanceof ReferenceCamera && cameraId.equals(camera.getId())) {
                return (ReferenceCamera) camera;
            }
        }
        return null;
    }

    private String getCameraName() {
        return calibrationCamera == null ? "null" : calibrationCamera.getName();
    }

    private static String getAxisName(Axis axis) {
        return axis == null ? "null" : axis.getName();
    }

    private static String formatLocation(Location location) {
        if (location == null) {
            return "null";
        }
        Location mm = location.convertToUnits(LengthUnit.Millimeters);
        return String.format(Locale.US, "(x=%.6f,y=%.6f,z=%.6f,r=%.6f)",
                mm.getX(), mm.getY(), mm.getZ(), mm.getRotation());
    }

    @Override
    public Wizard getConfigurationWizard() {
        return new ReferenceGridTransformAxisConfigurationWizard((AbstractMachine) Configuration.get().getMachine(), this);
    }
}
