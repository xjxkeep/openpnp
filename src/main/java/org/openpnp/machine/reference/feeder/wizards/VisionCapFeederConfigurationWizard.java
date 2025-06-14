/*
 * Copyright (C) 2011 Jason von Nieda <jason@vonnieda.org>
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

 package org.openpnp.machine.reference.feeder.wizards;

 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.concurrent.Callable;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.border.TitledBorder;
 
 import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
 import org.opencv.core.Mat;
 import org.opencv.core.Point;
 import org.opencv.imgproc.Imgproc;
 import org.openpnp.Translations;
 import org.openpnp.gui.MainFrame;
 import org.openpnp.gui.components.CameraView;
 import org.openpnp.gui.components.CameraViewActionEvent;
 import org.openpnp.gui.components.CameraViewActionListener;
 import org.openpnp.gui.components.CameraViewFilter;
 import org.openpnp.gui.components.ComponentDecorators;
 import org.openpnp.gui.components.LocationButtonsPanel;
 import org.openpnp.gui.support.AbstractConfigurationWizard;
 import org.openpnp.gui.support.DoubleConverter;
 import org.openpnp.gui.support.Helpers;
 import org.openpnp.gui.support.IdentifiableListCellRenderer;
 import org.openpnp.gui.support.IntegerConverter;
 import org.openpnp.gui.support.LengthConverter;
 import org.openpnp.gui.support.MessageBoxes;
 import org.openpnp.gui.support.MutableLocationProxy;
 import org.openpnp.gui.support.PartsComboBoxModel;
 import org.openpnp.machine.reference.camera.BufferedImageCamera;
 import org.openpnp.machine.reference.feeder.VisionCapFeeder;
 import org.openpnp.machine.reference.feeder.VisionCapFeeder.TapeType;
 import org.openpnp.model.Board;
 import org.openpnp.model.Configuration;
 import org.openpnp.model.Length;
 import org.openpnp.model.LengthUnit;
 import org.openpnp.model.Location;
 import org.openpnp.model.Part;
 import org.openpnp.model.Placement;
 import org.openpnp.spi.Camera;
 import org.openpnp.util.HslColor;
 import org.openpnp.util.MovableUtils;
 import org.openpnp.util.OpenCvUtils;
 import org.openpnp.util.UiUtils;
 import org.openpnp.util.VisionUtils;
 import org.openpnp.vision.FluentCv;
 import org.openpnp.vision.Ransac;
 import org.openpnp.vision.pipeline.CvPipeline;
 import org.openpnp.vision.pipeline.CvStage;
 import org.openpnp.vision.pipeline.ui.CvPipelineEditor;
 import org.openpnp.vision.pipeline.ui.CvPipelineEditorDialog;
 import org.pmw.tinylog.Logger;
 
 import com.google.common.collect.Lists;
 import com.google.common.util.concurrent.FutureCallback;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.FormSpecs;
 import com.jgoodies.forms.layout.RowSpec;
 
 @SuppressWarnings("serial")
 public class VisionCapFeederConfigurationWizard extends AbstractConfigurationWizard {
     private final VisionCapFeeder feeder;
 
     private JPanel panelPart;
 
     private JComboBox comboBoxPart;
     private JLabel lblPartInfo;
     
     private JTextField textFieldFeedStartX;
     private JTextField textFieldFeedStartY;
     private JTextField textFieldFeedStartZ;
     private JTextField textFieldFeedEndX;
     private JTextField textFieldFeedEndY;
     private JTextField textFieldFeedEndZ;
     private JTextField textFieldTapeWidth;
     private JLabel lblPartPitch;
     private JTextField textFieldPartPitch;
     private JPanel panelTapeSettings;
     private JPanel panelLocations;
     private LocationButtonsPanel locationButtonsPanelFeedStart;
     private LocationButtonsPanel locationButtonsPanelFeedEnd;
     private JLabel lblFeedCount;
     private JTextField textFieldFeedCount;
     private JButton btnResetFeedCount;
     private JLabel lblMaxFeedCount;
     private JButton btnMaxFeedCount;
     private JTextField textFieldMaxFeedCount;
     private JLabel lblTapeType;
     private JComboBox comboBoxTapeType;
     private JLabel lblRotationInTape;
     private JTextField textFieldLocationRotation;
     private JButton btnAutoSetup;
     private JCheckBox chckbxUseVision;
     private JLabel lblUseVision;
     private JLabel lblPart;
     private JLabel lblRetryCount;
     private JTextField retryCountTf;
     private JLabel lblDetectX;
     private JTextField textFieldDetectX;
     private JLabel lblDetectY;
     private JTextField textFieldDetectY;
     private JLabel lblDetectWidth;
     private JTextField textFieldDetectWidth;
     private JLabel lblDetectHight;
     private JTextField textFieldDetectHight;
 
     private boolean logDebugInfo = false;
     private Location firstPartLocation;
     private Location secondPartLocation;
     private List<Location> part1HoleLocations;
     private Camera autoSetupCamera;
 
 
     public VisionCapFeederConfigurationWizard(VisionCapFeeder feeder) {
         this.feeder = feeder;
 
         panelPart = new JPanel();
         panelPart.setBorder(new TitledBorder(null, Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.PanelPart.Border.title"), //$NON-NLS-1$
                 TitledBorder.LEADING, TitledBorder.TOP, null));
         contentPanel.add(panelPart);
         panelPart.setLayout(new FormLayout(new ColumnSpec[] {
                 FormSpecs.RELATED_GAP_COLSPEC,
                 FormSpecs.DEFAULT_COLSPEC,
                 FormSpecs.RELATED_GAP_COLSPEC,
                 FormSpecs.DEFAULT_COLSPEC,
                 FormSpecs.RELATED_GAP_COLSPEC,
                 FormSpecs.DEFAULT_COLSPEC,
                 FormSpecs.RELATED_GAP_COLSPEC,
                 ColumnSpec.decode("default:grow"),},
             new RowSpec[] {
                 FormSpecs.RELATED_GAP_ROWSPEC,
                 FormSpecs.DEFAULT_ROWSPEC,
                 FormSpecs.RELATED_GAP_ROWSPEC,
                 FormSpecs.DEFAULT_ROWSPEC,
                 FormSpecs.RELATED_GAP_ROWSPEC,
                 FormSpecs.DEFAULT_ROWSPEC,
                 FormSpecs.RELATED_GAP_ROWSPEC,
                 FormSpecs.DEFAULT_ROWSPEC,}));
         try {
         }
         catch (Throwable t) {
             // Swallow this error. This happens during parsing in
             // in WindowBuilder but doesn't happen during normal run.
         }
 
         lblPart = new JLabel(Translations.getString("ReferenceStripFeederConfigurationWizard.PartLabel.text")); //$NON-NLS-1$
         panelPart.add(lblPart, "2, 2, right, default");
 
         comboBoxPart = new JComboBox();
         comboBoxPart.setModel(new PartsComboBoxModel());
         comboBoxPart.setRenderer(new IdentifiableListCellRenderer<Part>());
         panelPart.add(comboBoxPart, "4, 2, 3, 1, left, default");
 
         comboBoxPart.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 updatePartInfo(e);            
             }
         });
         
         lblPartInfo = new JLabel(" ");
         panelPart.add(lblPartInfo,"8, 2, left, default");
         
         lblRotationInTape = new JLabel(Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.RotationInTapeLabel.text")); //$NON-NLS-1$
         lblRotationInTape.setToolTipText("<html>\n<p>The <strong>Rotation in Tape</strong> setting must be interpreted relative to the tape's orientation, <br/>\nregardless of how the feeder/tape is oriented on the machine. </p>\n<ol>\n<li>\n<p>Look at the <strong>neutral</strong> upright orientation of the part package/footprint <br/>\nas drawn inside your E-CAD <strong>library</strong>.</p>\n</li>\n<li>\n<p>Note how pin 1, polarity, cathode etc. are oriented.  <br/>\nThis is your 0° for the part.</p>\n</li>\n<li>\n<p>Look at the tape so that the sprocket holes are at the top. <br/>\nThis is your 0° tape orientation (per EIA-481 industry standard).</p>\n</li>\n<li>\n<p>Determine how the part is rotated inside the tape pocket, <em>relative</em> from  <br/>\nits upright orientation in (1).  Positive rotation goes counter-clockwise.<br/>\nThis is your <strong>Rotation in Tape</strong>.</p>\n</li>\n</ol>\n</html>");
         panelPart.add(lblRotationInTape, "2, 4, left, default");
 
         textFieldLocationRotation = new JTextField();
         panelPart.add(textFieldLocationRotation, "4, 4, fill, default");
         textFieldLocationRotation.setColumns(4);
 
         lblRetryCount = new JLabel(Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.FeedRetryCountLabel.text")); //$NON-NLS-1$
         panelPart.add(lblRetryCount, "2, 6, right, default");
 
         retryCountTf = new JTextField();
         retryCountTf.setText("3");
         panelPart.add(retryCountTf, "4, 6, fill, default");
         retryCountTf.setColumns(3);
         
         lblPickRetryCount = new JLabel(Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.PickRetryCountLabel.text")); //$NON-NLS-1$
         panelPart.add(lblPickRetryCount, "2, 8, right, default");
         
         pickRetryCount = new JTextField();
         pickRetryCount.setText("3");
         pickRetryCount.setColumns(3);
         panelPart.add(pickRetryCount, "4, 8, fill, default");
 
         panelTapeSettings = new JPanel();
         contentPanel.add(panelTapeSettings);
         panelTapeSettings.setBorder(new TitledBorder(null,
                 Translations.getString("ReferenceStripFeederConfigurationWizard.PanelTapeSettings.Border.title"), //$NON-NLS-1$
                 TitledBorder.LEADING, TitledBorder.TOP, null));
         panelTapeSettings.setLayout(new FormLayout(
                 new ColumnSpec[] {FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                         FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                         FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                         FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                         FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                         FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,},
                 new RowSpec[] {
                         FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, // 1,2
                         FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, // 3,4
                         FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, // 5,6
                         FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, // 7,8
                         FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, // 9,10
                         FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, // 11,12
                         FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC  // 13,14
                 }
         ));
 
         btnAutoSetup = new JButton(autoSetup);
         panelTapeSettings.add(btnAutoSetup, "2, 2, 11, 1");
 
         lblTapeType = new JLabel(Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.TapeTypeLabel.text")); //$NON-NLS-1$
         panelTapeSettings.add(lblTapeType, "2, 4, right, default");
 
         comboBoxTapeType = new JComboBox(TapeType.values());
         panelTapeSettings.add(comboBoxTapeType, "4, 4, fill, default");
 
         JLabel lblTapeWidth = new JLabel(Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.TapeWidthLabel.text")); //$NON-NLS-1$
         panelTapeSettings.add(lblTapeWidth, "8, 4, right, default");
 
         textFieldTapeWidth = new JTextField();
         panelTapeSettings.add(textFieldTapeWidth, "10, 4");
         textFieldTapeWidth.setColumns(5);
 
         lblPartPitch = new JLabel(Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.PartPitchLabel.text")); //$NON-NLS-1$
         panelTapeSettings.add(lblPartPitch, "2, 6, right, default");
 
         textFieldPartPitch = new JTextField();
         panelTapeSettings.add(textFieldPartPitch, "4, 6");
         textFieldPartPitch.setColumns(5);
 
         lblFeedCount = new JLabel(Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.FeedCountLabel.text")); //$NON-NLS-1$
         panelTapeSettings.add(lblFeedCount, "8, 6, right, default");
 
         textFieldFeedCount = new JTextField();
         panelTapeSettings.add(textFieldFeedCount, "10, 6");
         textFieldFeedCount.setColumns(10);
 
         btnResetFeedCount = new JButton(new AbstractAction(Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.ResetFeedCountButton.text")) { //$NON-NLS-1$
             @Override
             public void actionPerformed(ActionEvent e) {
                 textFieldFeedCount.setText("0");
                 applyAction.actionPerformed(e);
             }
         });
         btnResetFeedCount.setToolTipText(Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.ResetFeedCountButton.toolTipText")); //$NON-NLS-1$
         panelTapeSettings.add(btnResetFeedCount, "12, 6");
 
         lblMaxFeedCount = new JLabel(Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.MaxFeedCountLabel.text")); //$NON-NLS-1$
         panelTapeSettings.add(lblMaxFeedCount,"8, 8, right, default");
         textFieldMaxFeedCount = new JTextField();
         panelTapeSettings.add(textFieldMaxFeedCount,"10,8");
         textFieldMaxFeedCount.setColumns(10);
         textFieldMaxFeedCount.setToolTipText(Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.MaxFeedCountTextField.toolTipText")); //$NON-NLS-1$
         btnMaxFeedCount = new JButton(new AbstractAction(Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.AutoSetMaxFeedCountButton.text")) { //$NON-NLS-1$
             @Override
             public void actionPerformed(ActionEvent e) {
                 Location h0 = feeder.getReferenceHoleLocation();
                 Location h1 = feeder.getLastHoleLocation();
                 Length strip_len = h0.getLinearLengthTo(h1);
                 double part_count = strip_len.divide(feeder.getPartPitch());
                 int ipart_count = 1+(int)Math.round(part_count);
                 textFieldMaxFeedCount.setText(Integer.toString(ipart_count));
             }
         });
         btnMaxFeedCount.setToolTipText(Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.AutoSetMaxFeedCountButton.toolTipText")); //$NON-NLS-1$
         panelTapeSettings.add(btnMaxFeedCount,"12,8");
         
         lblDetectX = new JLabel("Detect X");
         panelTapeSettings.add(lblDetectX, "2, 10, right, default");
         textFieldDetectX = new JTextField();
         panelTapeSettings.add(textFieldDetectX, "4, 10");
         textFieldDetectX.setColumns(5);

         lblDetectY = new JLabel("Detect Y");
         panelTapeSettings.add(lblDetectY, "8, 10, right, default");
         textFieldDetectY = new JTextField();
         panelTapeSettings.add(textFieldDetectY, "10, 10");
         textFieldDetectY.setColumns(5);

         lblDetectWidth = new JLabel("Detect Width");
         panelTapeSettings.add(lblDetectWidth, "2, 12, right, default");
         textFieldDetectWidth = new JTextField();
         panelTapeSettings.add(textFieldDetectWidth, "4, 12");
         textFieldDetectWidth.setColumns(5);

         lblDetectHight = new JLabel("Detect Hight");
         panelTapeSettings.add(lblDetectHight, "8, 12, right, default");
         textFieldDetectHight = new JTextField();
         panelTapeSettings.add(textFieldDetectHight, "10, 12");
         textFieldDetectHight.setColumns(5);
         
         JPanel panelVision = new JPanel();
         panelVision.setBorder(new TitledBorder(null, Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.PanelVision.Border.title"), //$NON-NLS-1$
                 TitledBorder.LEADING, TitledBorder.TOP, null, null));
         contentPanel.add(panelVision);
         panelVision.setLayout(new FormLayout(
                 new ColumnSpec[] {FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                         FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                         FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC},
                 new RowSpec[] {FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                         FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,}));
 
         lblUseVision = new JLabel(Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.PanelVision.UseVisionLabel.text")); //$NON-NLS-1$
         panelVision.add(lblUseVision, "2, 2");
 
         chckbxUseVision = new JCheckBox("");
         panelVision.add(chckbxUseVision, "4, 2");
 
         JButton btnEditPipeline = new JButton(Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.PanelVision.EditPipelineButton.text")); //$NON-NLS-1$
         btnEditPipeline.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 UiUtils.messageBoxOnException(() -> {
                     editPipeline();
                 });
             }
         });
         panelVision.add(btnEditPipeline, "2, 4");
 
         JButton btnResetPipeline = new JButton(Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.PanelVision.ResetPipelineButton.text")); //$NON-NLS-1$
         btnResetPipeline.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 resetPipeline();
             }
         });
         panelVision.add(btnResetPipeline, "4, 4");
 
         JButton btnClearVisionCache = new JButton(Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.PanelVision.ClearVisionCacheButton.text")); //$NON-NLS-1$
         btnClearVisionCache.setToolTipText(Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.PanelVision.ClearVisionCacheButton.toolTipText")); //$NON-NLS-1$
         btnClearVisionCache.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 clearVisionCache();
             }
         });
         panelVision.add(btnClearVisionCache, "6, 4");
 
         panelLocations = new JPanel();
         contentPanel.add(panelLocations);
         panelLocations.setBorder(new TitledBorder(null, Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.PanelLocations.Border.title"), //$NON-NLS-1$
                 TitledBorder.LEADING, TitledBorder.TOP, null, null));
         panelLocations.setLayout(new FormLayout(
                 new ColumnSpec[] {FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                         FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                         FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                         FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                         FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:default:grow"),},
                 new RowSpec[] {FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                         FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                         FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,}));
 
         JLabel lblX = new JLabel("X");
         panelLocations.add(lblX, "4, 2");
 
         JLabel lblY = new JLabel("Y");
         panelLocations.add(lblY, "6, 2");
 
         JLabel lblZ_1 = new JLabel("Z");
         panelLocations.add(lblZ_1, "8, 2");
 
         JLabel lblFeedStartLocation = new JLabel(Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.PanelLocations.ReferenceHoleLocationLabel.text")); //$NON-NLS-1$
         lblFeedStartLocation.setToolTipText(Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.PanelLocations.ReferenceHoleLocationLabel.toolTipText")); //$NON-NLS-1$
         panelLocations.add(lblFeedStartLocation, "2, 4, right, default");
 
         textFieldFeedStartX = new JTextField();
         panelLocations.add(textFieldFeedStartX, "4, 4");
         textFieldFeedStartX.setColumns(8);
 
         textFieldFeedStartY = new JTextField();
         panelLocations.add(textFieldFeedStartY, "6, 4");
         textFieldFeedStartY.setColumns(8);
 
         textFieldFeedStartZ = new JTextField();
         panelLocations.add(textFieldFeedStartZ, "8, 4");
         textFieldFeedStartZ.setColumns(8);
 
         locationButtonsPanelFeedStart = new LocationButtonsPanel(textFieldFeedStartX,
                 textFieldFeedStartY, textFieldFeedStartZ, null);
         panelLocations.add(locationButtonsPanelFeedStart, "10, 4");
 
         JLabel lblFeedEndLocation = new JLabel(Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.PanelLocations.NextHoleLocationLabel.text")); //$NON-NLS-1$
         lblFeedEndLocation.setToolTipText(Translations.getString(
                 "ReferenceStripFeederConfigurationWizard.PanelLocations.NextHoleLocationLabel.toolTipText")); //$NON-NLS-1$
         panelLocations.add(lblFeedEndLocation, "2, 6, right, default");
 
         textFieldFeedEndX = new JTextField();
         panelLocations.add(textFieldFeedEndX, "4, 6");
         textFieldFeedEndX.setColumns(8);
 
         textFieldFeedEndY = new JTextField();
         panelLocations.add(textFieldFeedEndY, "6, 6");
         textFieldFeedEndY.setColumns(8);
 
 //        textFieldFeedEndZ = new JTextField();
 //        panelLocations.add(textFieldFeedEndZ, "8, 6");
 //        textFieldFeedEndZ.setColumns(8);
 
         locationButtonsPanelFeedEnd = new LocationButtonsPanel(textFieldFeedEndX, textFieldFeedEndY,
                 null, //textFieldFeedEndZ, 
                 null);
         panelLocations.add(locationButtonsPanelFeedEnd, "10, 6");
     }
 
     @Override
     public void createBindings() {
         LengthConverter lengthConverter = new LengthConverter();
         IntegerConverter intConverter = new IntegerConverter();
         DoubleConverter doubleConverter = new DoubleConverter(Configuration.get()
                                                                            .getLengthDisplayFormat());
 
         MutableLocationProxy location = new MutableLocationProxy();
         bind(UpdateStrategy.READ_WRITE, feeder, "location", location, "location");
         addWrappedBinding(location, "rotation", textFieldLocationRotation, "text", doubleConverter);
 
         addWrappedBinding(feeder, "part", comboBoxPart, "selectedItem");
         addWrappedBinding(feeder, "feedRetryCount", retryCountTf, "text", intConverter);
         addWrappedBinding(feeder, "pickRetryCount", pickRetryCount, "text", intConverter);
         addWrappedBinding(feeder, "tapeType", comboBoxTapeType, "selectedItem");
 
         addWrappedBinding(feeder, "tapeWidth", textFieldTapeWidth, "text", lengthConverter);
         addWrappedBinding(feeder, "partPitch", textFieldPartPitch, "text", lengthConverter);
         addWrappedBinding(feeder, "feedCount", textFieldFeedCount, "text", intConverter);
         addWrappedBinding(feeder, "maxFeedCount", textFieldMaxFeedCount, "text", intConverter);
         addWrappedBinding(feeder, "detectX", textFieldDetectX, "text", lengthConverter);
         addWrappedBinding(feeder, "detectY", textFieldDetectY, "text", lengthConverter);
         addWrappedBinding(feeder, "detectWidth", textFieldDetectWidth, "text", lengthConverter);
         addWrappedBinding(feeder, "detectHight", textFieldDetectHight, "text", lengthConverter);
 
         MutableLocationProxy feedStartLocation = new MutableLocationProxy();
         bind(UpdateStrategy.READ_WRITE, feeder, "referenceHoleLocation", feedStartLocation,
                 "location");
         addWrappedBinding(feedStartLocation, "lengthX", textFieldFeedStartX, "text",
                 lengthConverter);
         addWrappedBinding(feedStartLocation, "lengthY", textFieldFeedStartY, "text",
                 lengthConverter);
         addWrappedBinding(feedStartLocation, "lengthZ", textFieldFeedStartZ, "text",
                 lengthConverter);
 
         MutableLocationProxy feedEndLocation = new MutableLocationProxy();
         bind(UpdateStrategy.READ_WRITE, feeder, "lastHoleLocation", feedEndLocation, "location");
         addWrappedBinding(feedEndLocation, "lengthX", textFieldFeedEndX, "text", lengthConverter);
         addWrappedBinding(feedEndLocation, "lengthY", textFieldFeedEndY, "text", lengthConverter);
 //        addWrappedBinding(feedEndLocation, "lengthZ", textFieldFeedEndZ, "text", lengthConverter);
 
         addWrappedBinding(feeder, "visionEnabled", chckbxUseVision, "selected");
 
         ComponentDecorators.decorateWithAutoSelect(textFieldLocationRotation);
         ComponentDecorators.decorateWithAutoSelectAndLengthConversion(textFieldTapeWidth);
         ComponentDecorators.decorateWithAutoSelectAndLengthConversion(textFieldDetectHight);
         ComponentDecorators.decorateWithAutoSelectAndLengthConversion(textFieldDetectWidth);
         ComponentDecorators.decorateWithAutoSelectAndLengthConversion(textFieldDetectX);
         ComponentDecorators.decorateWithAutoSelectAndLengthConversion(textFieldDetectY);
         ComponentDecorators.decorateWithAutoSelect(retryCountTf);
         ComponentDecorators.decorateWithAutoSelect(pickRetryCount);
         ComponentDecorators.decorateWithAutoSelectAndLengthConversion(textFieldPartPitch);
         ComponentDecorators.decorateWithAutoSelect(textFieldFeedCount);
         ComponentDecorators.decorateWithAutoSelect(textFieldMaxFeedCount);
         ComponentDecorators.decorateWithAutoSelectAndLengthConversion(textFieldFeedStartX);
         ComponentDecorators.decorateWithAutoSelectAndLengthConversion(textFieldFeedStartY);
         ComponentDecorators.decorateWithAutoSelectAndLengthConversion(textFieldFeedStartZ);
         ComponentDecorators.decorateWithAutoSelectAndLengthConversion(textFieldFeedEndX);
         ComponentDecorators.decorateWithAutoSelectAndLengthConversion(textFieldFeedEndY);
 //        ComponentDecorators.decorateWithAutoSelectAndLengthConversion(textFieldFeedEndZ);
     }
 
     private void updatePartInfo(ActionEvent e)
     {
         int count = 0;
         Part feeder_part =(Part)comboBoxPart.getSelectedItem(); 
     
         for (Board board: Configuration.get().getBoards())    {
             for (Placement p : board.getPlacements())
             {
                 if (p.getPart() == feeder_part)
                 {
                     count++;
                 }
             }
         }
         if (count > 0)
         {
             String lbl = Integer.toString(count) + " used by current job";
             lblPartInfo.setText(lbl);
         }
         else
         {
             lblPartInfo.setText("");
         }
     }
     
     private Action autoSetup = new AbstractAction(Translations.getString(
             "ReferenceStripFeederConfigurationWizard.Action.AutoSetup")) { //$NON-NLS-1$
         @Override
         public void actionPerformed(ActionEvent e) {
             try {
                 autoSetupCamera = Configuration.get()
                                                .getMachine()
                                                .getDefaultHead()
                                                .getDefaultCamera();
                 if (autoSetupCamera.isUnitsPerPixelAtZCalibrated()) {
                     // We need 3D units per pixel, make sure the Z is set first.
                     LengthConverter lengthConverter = new LengthConverter();
                     Length z = lengthConverter.convertReverse(textFieldFeedStartZ.getText());
                     if (!z.isInitialized()) {
                         throw new Exception("Please set the Reference Hole Location Z coordinate first.");
                     }
                     autoSetupCamera.moveTo(autoSetupCamera.getLocation()
                             .deriveLengths(null, null, z, null));
                 }
             }
             catch (Exception ex) {
                 MessageBoxes.errorBox(getTopLevelAncestor(), "Auto Setup Failure", ex);
                 return;
             }
 
             btnAutoSetup.setAction(autoSetupCancel);
 
             CameraView cameraView = MainFrame.get()
                                              .getCameraViews()
                                              .getCameraView(autoSetupCamera);
             cameraView.addActionListener(autoSetupPart1Clicked);
             cameraView.setText("Click on the center of the first part in the tape.");
             MovableUtils.fireTargetedUserAction(autoSetupCamera);
             cameraView.flash();
 
             logDebugInfo = (e.getModifiers() & ActionEvent.ALT_MASK) != 0;
 
             cameraView.setCameraViewFilter(new CameraViewFilter() {
                 private boolean hasShownError = false;
 
                 @Override
                 public BufferedImage filterCameraImage(Camera camera, BufferedImage image) {
                     try {
                         BufferedImage bufferedImage = showHoles(camera, image);
                         hasShownError = false;
                         return bufferedImage;
                     }
                     catch (Exception e) {
                         if (!hasShownError) {
                             hasShownError = true;
                             UiUtils.showError(e);
                         }
                         else {
                             Logger.debug("{}: {}", "Error", e);
                         }
                     }
 
                     return null;
                 }
             });
         }
     };
 
     private Action autoSetupCancel = new AbstractAction(Translations.getString(
             "ReferenceStripFeederConfigurationWizard.Action.AutoSetupCancel")) { //$NON-NLS-1$
         @Override
         public void actionPerformed(ActionEvent e) {
             btnAutoSetup.setAction(autoSetup);
             CameraView cameraView = MainFrame.get()
                                              .getCameraViews()
                                              .getCameraView(autoSetupCamera);
             cameraView.setText(null);
             cameraView.setCameraViewFilter(null);
             cameraView.removeActionListener(autoSetupPart1Clicked);
             cameraView.removeActionListener(autoSetupPart2Clicked);
         }
     };
 
     private CameraViewActionListener autoSetupPart1Clicked = new CameraViewActionListener() {
         @Override
         public void actionPerformed(final CameraViewActionEvent action) {
             firstPartLocation = action.getLocation();
             final CameraView cameraView = MainFrame.get()
                                                    .getCameraViews()
                                                    .getCameraView(autoSetupCamera);
             cameraView.removeActionListener(this);
             Configuration.get()
                          .getMachine()
                          .submit(new Callable<Void>() {
                              public Void call() throws Exception {
                                  cameraView.setText("Checking first part...");
                                  autoSetupCamera.moveTo(firstPartLocation);
                                  MovableUtils.fireTargetedUserAction(autoSetupCamera);
                                  part1HoleLocations = findHoles(autoSetupCamera);
                                  if (part1HoleLocations.size() < 1) {
                                      throw new Exception("No hole found at selected location");
                                  }
 
                                  cameraView.setText(
                                          "Now click on the center of the second part in the tape.");
                                  cameraView.flash();
 
                                  cameraView.addActionListener(autoSetupPart2Clicked);
                                  return null;
                              }
                          }, new FutureCallback<Void>() {
                              @Override
                              public void onSuccess(Void result) {}
 
                              @Override
                              public void onFailure(final Throwable t) {
                                  SwingUtilities.invokeLater(new Runnable() {
                                      public void run() {
                                          autoSetupCancel.actionPerformed(null);
                                          MessageBoxes.errorBox(getTopLevelAncestor(),
                                                  "Auto Setup Failure", t);
                                      }
                                  });
                              }
                          });
         }
     };
 
     private CameraViewActionListener autoSetupPart2Clicked = new CameraViewActionListener() {
         @Override
         public void actionPerformed(final CameraViewActionEvent action) {
             secondPartLocation = action.getLocation();
             final CameraView cameraView = MainFrame.get()
                                                    .getCameraViews()
                                                    .getCameraView(autoSetupCamera);
             cameraView.removeActionListener(this);
             Configuration.get()
                          .getMachine()
                          .submit(new Callable<Void>() {
                              public Void call() throws Exception {
                                  cameraView.setText("Checking second part...");
                                  autoSetupCamera.moveTo(secondPartLocation);
                                  MovableUtils.fireTargetedUserAction(autoSetupCamera);
                                  List<Location> part2HoleLocations = findHoles(autoSetupCamera);
                                  if (part2HoleLocations.size() < 1) {
                                      throw new Exception("No hole found at selected location");
                                  }
 
                                  List<Location> referenceHoles = deriveReferenceHoles(
                                          firstPartLocation, secondPartLocation,
                                          part1HoleLocations, part2HoleLocations);
                                  final Location referenceHole1 = referenceHoles.get(0)
                                          .derive(null, null,
                                                  null, 0d)
                                          .derive(feeder.getReferenceHoleLocation(), 
                                                  false,  false,  true, false);
                                  final Location referenceHole2 = referenceHoles.get(1)
                                          .derive(null, null,
                                                  null, 0d)
                                          .derive(feeder.getLastHoleLocation(), 
                                                  false,  false,  true, false);
 
                                  feeder.setReferenceHoleLocation(referenceHole1);
                                  feeder.setLastHoleLocation(referenceHole2);
 
                                  Length partPitch = firstPartLocation.getLinearLengthTo(secondPartLocation);
                                  // Round to the nearest 2mm (parts are spaced either 2mm or 4mm in the tape)
                                  Length partPitchMM = partPitch.convertToUnits(LengthUnit.Millimeters);
                                  long standardPitchIncrements = Math.round(partPitchMM.getValue() / 2.0);
                                  if (standardPitchIncrements == 0) {
                                      throw new Exception("The same part was selected both times");
                                  }
                                  partPitchMM.setValue(2.0 * standardPitchIncrements);
 
                                  final Length partPitch_ = partPitchMM.convertToUnits(firstPartLocation.getUnits());
                                  SwingUtilities.invokeLater(new Runnable() {
                                      public void run() {
                                          Helpers.copyLocationIntoTextFields(referenceHole1,
                                                  textFieldFeedStartX, textFieldFeedStartY, null);
                                          Helpers.copyLocationIntoTextFields(referenceHole2,
                                                  textFieldFeedEndX, textFieldFeedEndY, null);
                                          textFieldPartPitch.setText(partPitch_.getValue() + "");
                                      }
                                  });
 
                                  feeder.setFeedCount(1);
                                  autoSetupCamera.moveTo(feeder.getPickLocation());
                                  MovableUtils.fireTargetedUserAction(autoSetupCamera);
                                  feeder.setFeedCount(0);
 
                                  cameraView.setText("Setup complete!");
                                  Thread.sleep(1500);
                                  cameraView.setText(null);
                                  cameraView.setCameraViewFilter(null);
                                  btnAutoSetup.setAction(autoSetup);
 
                                  return null;
                              }
                          }, new FutureCallback<Void>() {
                              @Override
                              public void onSuccess(Void result) {}
 
                              @Override
                              public void onFailure(final Throwable t) {
                                  SwingUtilities.invokeLater(new Runnable() {
                                      public void run() {
                                          autoSetupCancel.actionPerformed(null);
                                          MessageBoxes.errorBox(getTopLevelAncestor(),
                                                  "Auto Setup Failure", t);
                                      }
                                  });
                              }
                          });
         }
     };
     private JLabel lblPickRetryCount;
     private JTextField pickRetryCount;
 
     private List<Location> findHoles(Camera camera) throws Exception {
         // Process the pipeline to clean up the image and detect the tape holes
         try (CvPipeline pipeline = getCvPipeline(camera, true)) {
             pipeline.process();
             
             // Grab the results
             FindHoles findHolesResults = new FindHoles(camera, pipeline).invoke();
             List<CvStage.Result.Circle> inLine = findHolesResults.getInLine();
             if (inLine.isEmpty()) {
                 throw new Exception("Feeder " + getName() + ": No tape holes found.");
             }
     
             List<Location> holeLocations = new ArrayList<>();
             for (int i=0; i<inLine.size(); i++) {
                 CvStage.Result.Circle result = inLine.get(i);
                 Location location = VisionUtils.getPixelLocation(camera, result.x, result.y);
                 holeLocations.add(location);
             }
     
             return holeLocations;
         }
     }
 
     /**
      * Show candidate holes in the image. Red are any holes that are found. Orange are holes that
      * passed the distance check but failed the line check. Blue are holes that passed the line
      * check but are considered superfluous. Green passed all checks and are considered good.
      * 
      * @param camera
      * @param image
      * @return
      */
     private BufferedImage showHoles(Camera camera, BufferedImage image) throws Exception {
         // BufferedCameraImage is used as we want to run the pipeline on an existing image
         BufferedImageCamera bufferedImageCamera = BufferedImageCamera.get(camera, image);
 
         try (CvPipeline pipeline = getCvPipeline(bufferedImageCamera, true)) {
             // Process the pipeline to clean up the image and detect the tape holes
             pipeline.process();
             // Grab the results
             Mat resultMat = pipeline.getWorkingImage().clone();
             FindHoles findHolesResults = new FindHoles(camera, pipeline).invoke();
     
             List<CvStage.Result.Circle> inLine = findHolesResults.getInLine();
             List<Ransac.Line> lines = findHolesResults.getLines();
             Ransac.Line bestLine = findHolesResults.getBestLine();
     
             drawLines(resultMat, lines, Color.orange, 1);
             if (bestLine != null) {
                 drawLine(resultMat, bestLine, Color.yellow, 2);
             }
             drawCircles(resultMat, inLine, inLine.size(), Color.blue);
             drawCircles(resultMat, inLine, 2, Color.green);
     
             BufferedImage showResult = OpenCvUtils.toBufferedImage(resultMat);
             resultMat.release();
             return showResult;
         }
     }
 
     private class FindHoles {
         private Camera camera;
         private CvPipeline pipeline;
         private List<Ransac.Line> lines;
         private Ransac.Line bestLine;
         private List<CvStage.Result.Circle> inLine;
 
         public FindHoles(Camera camera, CvPipeline pipeline) {
             this.camera = camera;
             this.pipeline = pipeline;
         }
 
         public List<Ransac.Line> getLines() {
             return lines;
         }
 
         public Ransac.Line getBestLine() {
             return bestLine;
         }
 
         public List<CvStage.Result.Circle> getInLine() {
             return inLine;
         }
 
         public FindHoles invoke() throws Exception {
             List<CvStage.Result.Circle> results = pipeline.getExpectedResult(VisionUtils.PIPELINE_RESULTS_NAME)
                     .getExpectedListModel(CvStage.Result.Circle.class, null);
 
             // Sort by the distance to the camera center (which is over the part, not the hole)
             results.sort((a, b) -> {
                 Double da = VisionUtils.getPixelLocation(camera, a.x, a.y)
                         .getLinearDistanceTo(camera.getLocation());
                 Double db = VisionUtils.getPixelLocation(camera, b.x, b.y)
                         .getLinearDistanceTo(camera.getLocation());
                 return da.compareTo(db);
             });
 
             double maxDistanceToLine = VisionUtils.toPixels(feeder.getHoleLineDistanceMax(), camera);
             double minDistancePx = VisionUtils.toPixels(feeder.getHoleDistanceMin(), camera);
             double maxDistancePx = VisionUtils.toPixels(feeder.getHoleDistanceMax(), camera);
             double holePitchPx = VisionUtils.toPixels(feeder.getHolePitch(), camera);
             double minHolePitchPx = VisionUtils.toPixels(feeder.getHolePitchMin(), camera);
 
             List<Point> points = new ArrayList<>();
             // collect the circles into a list of points
             for (int i = 0; i < results.size(); i++) {
                 CvStage.Result.Circle circle = results.get(i);
                 points.add(new Point(circle.x, circle.y));
             }
 
             lines = Ransac.ransac(points, 100, maxDistanceToLine, holePitchPx, holePitchPx - minHolePitchPx, true);
 
             bestLine = null;
             for (Ransac.Line line : lines) {
                 Point a = line.a;
                 Point b = line.b;
 
                 Location aLocation = VisionUtils.getPixelLocation(camera, a.x, a.y);
                 Location bLocation = VisionUtils.getPixelLocation(camera, b.x, b.y);
 
                 // Checks the distance to the line *segment*, as we expect the line to have the maximum extents that
                 // encompass all points (circles) that meet the criteria.
                 Double distance = camera.getLocation().getLinearDistanceToLineSegment(aLocation, bLocation);
                 Double distancePx = VisionUtils.toPixels(new Length(distance, camera.getLocation().getUnits()), camera);
                 // Min distance is because we're centered at the part, not the hole - so we need to ignore
                 // circles found in the part
                 if ((distancePx >= minDistancePx) && (distancePx <= maxDistancePx)) {
                     // Take the first line that is close enough, as the lines are ordered by length (descending),
                     // and we want the longest line (avoid the case of a really close but erroneous line that
                     // would likely only be made up of only 2 points).
                     bestLine = line;
                     break;
                 }
             }
 
             inLine = new ArrayList<CvStage.Result.Circle>();
             if (bestLine != null) {
                 // Filter the circles by distance from the resulting line
                 List<CvStage.Result.Circle> realLine = new ArrayList<CvStage.Result.Circle>();
                 for (int i = 0; i < results.size(); i++) {
                     CvStage.Result.Circle circle = results.get(i);
 
                     Point p = new Point(circle.x, circle.y);
                     if (FluentCv.pointToLineDistance(bestLine.a, bestLine.b, p) <= maxDistanceToLine) {
                         realLine.add(circle);
                     }
                 }
 
                 // Compute the average offset from the ideal centre positions
                 Point a = bestLine.a;
                 Point b = bestLine.b;
                 Point ab = new Point(b.x - a.x, b.y - a.y);
                 double lineLen = Math.sqrt(ab.dot(ab));
                 double fittedLineLen = (double)Math.round(lineLen / holePitchPx) * holePitchPx;
                 Point lineDir = new Point(ab.x / lineLen, ab.y / lineLen);
 
                 Point totalOffsets = new Point();
                 for (CvStage.Result.Circle circle : realLine) {
                     Point p = new Point(circle.x, circle.y);
 
                     // Project p onto the line
                     Point ap = new Point(p.x - a.x, p.y - a.y);
                     double distAlongLine = ap.dot(lineDir) / lineDir.dot(lineDir);
 
                     double fittedLen = (double)Math.round(distAlongLine / holePitchPx) * holePitchPx;
                     Point fittedPos = new Point(a.x + lineDir.x * fittedLen, a.y + lineDir.y * fittedLen);
 
                     totalOffsets.x += fittedPos.x - p.x;
                     totalOffsets.y += fittedPos.y - p.y;
                 }
                 Point avgOffset = new Point(totalOffsets.x / realLine.size(), totalOffsets.y / realLine.size());
 
                 // Fit the detected circles to the best line at the expected spacing
                 Point fittedA = new Point(a.x - avgOffset.x, a.y - avgOffset.y);
                 for (CvStage.Result.Circle circle : realLine) {
                     Point p = new Point(circle.x, circle.y);
 
                     // Project p onto the line
                     Point ap = new Point(p.x - a.x, p.y - a.y);
                     double distAlongLine = ap.dot(lineDir) / lineDir.dot(lineDir);
 
                     double fittedLen = (double)Math.round(distAlongLine / holePitchPx) * holePitchPx;
                     Point fittedPosOnLine = new Point(lineDir.x * fittedLen, lineDir.y * fittedLen);
 
                     Point fittedP = new Point(fittedA.x + fittedPosOnLine.x, fittedA.y + fittedPosOnLine.y);
                     CvStage.Result.Circle fittedCircle = new CvStage.Result.Circle(fittedP.x, fittedP.y, circle.diameter);
                     inLine.add(fittedCircle);
                 }
             }
             return this;
         }
     }
 
     private void drawCircles(Mat mat, List<CvStage.Result.Circle> circles, int numToDraw, Color color) {
         Color centerColor = new HslColor(color).getComplementary();
 
         numToDraw = (numToDraw <= circles.size()) ? numToDraw : circles.size();
         for (int i=0; i<numToDraw; i++) {
             CvStage.Result.Circle circle = circles.get(i);
 
             double x = circle.x;
             double y = circle.y;
             double radius = circle.diameter / 2.0;
             Imgproc.circle(mat, new Point(x, y), (int) radius, FluentCv.colorToScalar(color), 2, Imgproc.LINE_AA);
             Imgproc.circle(mat, new Point(x, y), 1, FluentCv.colorToScalar(centerColor), 2, Imgproc.LINE_AA);
         }
     }
 
     private void drawLines(Mat mat, List<Ransac.Line> lines, Color color, int thickness) {
         for (Ransac.Line line : lines) {
             drawLine(mat, line, color, thickness);
         }
     }
 
     private void drawLine(Mat mat, Ransac.Line line, Color color, int thickness) {
         Imgproc.line(mat, line.a, line.b, FluentCv.colorToScalar(color), thickness);
     }
 
     private List<Location> deriveReferenceHoles(
             Location firstPartLocation,
             Location secondPartLocation,
             List<Location> part1HoleLocations,
             List<Location> part2HoleLocations) throws Exception {
 
         Location partsLocationRay = secondPartLocation.subtract(firstPartLocation);
         Point feedDirection = new Point(partsLocationRay.getX(), partsLocationRay.getY());
 
         // We are only interested in the pair of holes closest to each part
         part1HoleLocations = part1HoleLocations.subList(0, Math.min(2, part1HoleLocations.size()));
         part2HoleLocations = part2HoleLocations.subList(0, Math.min(2, part2HoleLocations.size()));
 
         // Part 2's reference hole is the one farthest from part 1, in the direction of part 2
         List<LocationsAlongRay> part2HoleLocationsAlongRay = new ArrayList<>(part2HoleLocations.size());
         for (Location partLocation : part2HoleLocations) {
             Location loc = partLocation.convertToUnits(partsLocationRay.getUnits());
             Point p = new Point(loc.getX() - firstPartLocation.getX(), loc.getY() - firstPartLocation.getY());
             double distanceAlongRay = feedDirection.dot(p);
             part2HoleLocationsAlongRay.add(new LocationsAlongRay(partLocation, distanceAlongRay));
         }
         part2HoleLocationsAlongRay.sort(null);
         List<Location> part2SortedLocations = new ArrayList<>(part2HoleLocationsAlongRay.size());
         part2HoleLocationsAlongRay.forEach(locationAlongRay -> part2SortedLocations.add(locationAlongRay.location));
         List<Location> part2ReverseSortedLocations = Lists.reverse(part2SortedLocations);
         Location part2ReferenceHole = part2ReverseSortedLocations.get(0);
 
         // Part 1's reference hole is the one closest to part 2's reference hole.
         List<Location> part1SortedLocations = VisionUtils.sortLocationsByDistance(part2ReferenceHole, part1HoleLocations);
         Location part1ReferenceHole = part1SortedLocations.get(0);
         double holePitchMin = feeder.getHolePitchMin().convertToUnits(part1ReferenceHole.getUnits()).getValue();
         double referenceHoleDistance = part1ReferenceHole.getLinearDistanceTo(part2ReferenceHole);
         // Part 1 and part 2 may have the same reference holes for 2mm part-pitch tape, so "the one closest to part 2's
         // reference hole" will be part 2's reference hole - thus we want the other hole
         if (referenceHoleDistance < holePitchMin) {
             part1ReferenceHole = part1SortedLocations.get(1);
         }
 
         // Get the vector perpendicular to feedDirection (perp dot is (-y, x) and gives the vector rotated 90° to the
         // left, but we want the vector rotated 90° to the right and thus (y, -x))
         Point expectedHoleHalfspace = new Point(feedDirection.y, -feedDirection.x);
         Location hole1RelativeLocation = part1ReferenceHole.subtract(firstPartLocation);
         Location hole2RelativeLocation = part2ReferenceHole.subtract(firstPartLocation);
         Point h1 = new Point(hole1RelativeLocation.getX(), hole1RelativeLocation.getY());
         Point h2 = new Point(hole2RelativeLocation.getX(), hole2RelativeLocation.getY());
         double h1Dist = expectedHoleHalfspace.dot(h1);
         double h2Dist = expectedHoleHalfspace.dot(h2);
         boolean correctOrientation = (h1Dist > 0.0) && (h2Dist > 0.0);
 
         if (this.logDebugInfo) {
             Logger.info("deriveReferenceHoles");
             Logger.info("  feedDirection: " + feedDirection);
             Logger.info("  firstPartLocation: " + firstPartLocation);
             Logger.info("  secondPartLocation: " + secondPartLocation);
             Logger.info("  part1HoleLocations: " + part1HoleLocations);
             Logger.info("  part2HoleLocations: " + part2HoleLocations);
             Logger.info("  part2HoleLocationsAlongRay: " + part2HoleLocationsAlongRay);
             Logger.info("  part2ReverseSortedLocations: " + part2ReverseSortedLocations);
             Logger.info("  part1SortedLocations: " + part1SortedLocations);
             Logger.info("  referenceHoleDistance: " + referenceHoleDistance);
             Logger.info("  h1Dist: " + h1Dist);
             Logger.info("  h2Dist: " + h2Dist);
             Logger.info("  correctOrientation: " + correctOrientation);
         }
 
         if (!correctOrientation) {
             throw new Exception("The tape is oriented incorrectly for the feed direction of the components selected");
         }
 
         List<Location> referenceHoles = new ArrayList<>();
         referenceHoles.add(part1ReferenceHole);
         referenceHoles.add(part2ReferenceHole);
         return referenceHoles;
     }
 
     private static class LocationsAlongRay implements Comparable<LocationsAlongRay> {
         public Location location;
         public double distance;
 
         public LocationsAlongRay(Location location, double distance) {
             this.location = location;
             this.distance = distance;
         }
 
         @Override
         public int compareTo(LocationsAlongRay o) {
             return Double.compare(this.distance, o.distance);
         }
 
         @Override
         public String toString() {
             return String.format(Locale.US, "(%s, %s)", Double.toString(distance), location.toString());
         }
     }
 
     private void editPipeline() throws Exception {
         Camera camera = Configuration.get().getMachine().getDefaultHead().getDefaultCamera();
         CvPipeline pipeline = getCvPipeline(camera, false);
         CvPipelineEditor editor = new CvPipelineEditor(pipeline);
         JDialog dialog = new CvPipelineEditorDialog(MainFrame.get(), feeder.getName() + " Pipeline", editor);
         dialog.setVisible(true);
     }
 
     private void resetPipeline() {
         feeder.resetPipeline();
     }
 
     private CvPipeline getCvPipeline(Camera camera, boolean clone) {
         Integer pxMinDistance = (int) VisionUtils.toPixels(feeder.getHolePitchMin(), camera);
         Integer pxMinDiameter = (int) VisionUtils.toPixels(feeder.getHoleDiameterMin(), camera);
         Integer pxMaxDiameter = (int) VisionUtils.toPixels(feeder.getHoleDiameterMax(), camera);
 
         try {
             CvPipeline pipeline = feeder.getPipeline();;
             if (clone) {
                 pipeline = pipeline.clone();
             }
             pipeline.setProperty("camera", camera);
             pipeline.setProperty("feeder", feeder);
             pipeline.setProperty("DetectFixedCirclesHough.minDistance", pxMinDistance);
             pipeline.setProperty("DetectFixedCirclesHough.minDiameter", pxMinDiameter);
             pipeline.setProperty("DetectFixedCirclesHough.maxDiameter", pxMaxDiameter);
             pipeline.setProperty("sprocketHole.diameter", feeder.getHoleDiameter());
             // Search Range is half camera. 
             Length range = camera.getWidth() > camera.getHeight() ? 
                     camera.getUnitsPerPixelAtZ().getLengthY().multiply(camera.getHeight()/2)
                     : camera.getUnitsPerPixelAtZ().getLengthX().multiply(camera.getWidth()/2);
             pipeline.setProperty("sprocketHole.maxDistance", range);
             return pipeline;
         }
         catch (CloneNotSupportedException e) {
             throw new Error(e);
         }
     }
 
     private void clearVisionCache() {
         feeder.resetVision();
     }
 }
 