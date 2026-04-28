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

package org.openpnp.machine.reference.axis.wizards;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.openpnp.gui.support.AxesComboBoxModel;
import org.openpnp.gui.support.NamedConverter;
import org.openpnp.machine.reference.axis.ReferenceGridTransformAxis;
import org.openpnp.machine.reference.camera.ReferenceCamera;
import org.openpnp.model.Configuration;
import org.openpnp.spi.Axis;
import org.openpnp.spi.Camera;
import org.openpnp.spi.LinearInputAxis;
import org.openpnp.spi.base.AbstractMachine;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings({ "rawtypes", "serial", "unchecked" })
public class ReferenceGridTransformAxisConfigurationWizard extends AbstractAxisConfigurationWizard {
    private final JPanel panelTransformation;
    private final JComboBox inputAxisX;
    private final JComboBox inputAxisY;
    private final JComboBox calibrationCamera;
    private final JCheckBox compensation;
    private final List<ReferenceCamera> referenceCameras;

    public ReferenceGridTransformAxisConfigurationWizard(AbstractMachine machine, ReferenceGridTransformAxis axis) {
        super(axis);

        referenceCameras = new ArrayList<>();
        for (Camera camera : machine.getAllCameras()) {
            if (camera instanceof ReferenceCamera) {
                referenceCameras.add((ReferenceCamera) camera);
            }
        }

        panelTransformation = new JPanel();
        panelTransformation.setBorder(new TitledBorder(null, "Grid Transformation", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        contentPanel.add(panelTransformation);
        panelTransformation.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("max(70dlu;default)"),
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("max(90dlu;default)"), },
            new RowSpec[] {
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC, }));

        panelTransformation.add(new JLabel("Input X"), "2, 2, right, default");
        inputAxisX = new JComboBox(new AxesComboBoxModel(machine, LinearInputAxis.class, Axis.Type.X, true));
        panelTransformation.add(inputAxisX, "4, 2, fill, default");

        panelTransformation.add(new JLabel("Input Y"), "2, 4, right, default");
        inputAxisY = new JComboBox(new AxesComboBoxModel(machine, LinearInputAxis.class, Axis.Type.Y, true));
        panelTransformation.add(inputAxisY, "4, 4, fill, default");

        panelTransformation.add(new JLabel("Calibration Camera"), "2, 6, right, default");
        calibrationCamera = new JComboBox(new DefaultComboBoxModel(referenceCameras.toArray(new ReferenceCamera[] {})));
        panelTransformation.add(calibrationCamera, "4, 6, fill, default");

        panelTransformation.add(new JLabel("Compensation?"), "2, 8, right, default");
        compensation = new JCheckBox();
        compensation.setToolTipText("<html>When enabled, SuppressStaticCompensation can bypass this grid during calibration and simulation moves.</html>");
        panelTransformation.add(compensation, "4, 8");
    }

    @Override
    public void createBindings() {
        super.createBindings();
        AbstractMachine machine = (AbstractMachine) Configuration.get().getMachine();
        NamedConverter<Axis> axisConverter = new NamedConverter<>(machine.getAxes());
        NamedConverter<ReferenceCamera> cameraConverter = new NamedConverter<>(referenceCameras);

        addWrappedBinding(getAxis(), "inputAxisX", inputAxisX, "selectedItem", axisConverter);
        addWrappedBinding(getAxis(), "inputAxisY", inputAxisY, "selectedItem", axisConverter);
        addWrappedBinding(getAxis(), "calibrationCamera", calibrationCamera, "selectedItem", cameraConverter);
        addWrappedBinding(getAxis(), "compensation", compensation, "selected");
    }
}
