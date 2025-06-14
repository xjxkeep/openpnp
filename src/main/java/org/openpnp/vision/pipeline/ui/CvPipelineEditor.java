package org.openpnp.vision.pipeline.ui;

import java.awt.BorderLayout;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.openpnp.util.UiUtils;
import org.openpnp.vision.pipeline.CvPipeline;
import org.openpnp.vision.pipeline.CvStage;
import org.openpnp.vision.pipeline.stages.ActuatorWrite;
import org.openpnp.vision.pipeline.stages.Add;
import org.openpnp.vision.pipeline.stages.AffineUnwarp;
import org.openpnp.vision.pipeline.stages.AffineWarp;
import org.openpnp.vision.pipeline.stages.BlurGaussian;
import org.openpnp.vision.pipeline.stages.BlurMedian;
import org.openpnp.vision.pipeline.stages.ClosestModel;
import org.openpnp.vision.pipeline.stages.ComposeResult;
import org.openpnp.vision.pipeline.stages.ConvertColor;
import org.openpnp.vision.pipeline.stages.ConvertModelToKeyPoints;
import org.openpnp.vision.pipeline.stages.ConvertModelToPoints;
import org.openpnp.vision.pipeline.stages.CreateFootprintTemplateImage;
import org.openpnp.vision.pipeline.stages.CreateModelTemplateImage;
import org.openpnp.vision.pipeline.stages.CreateShapeTemplateImage;
import org.openpnp.vision.pipeline.stages.DetectCirclesHough;
import org.openpnp.vision.pipeline.stages.DetectCircularSymmetry;
import org.openpnp.vision.pipeline.stages.DetectEdgesCanny;
import org.openpnp.vision.pipeline.stages.DetectEdgesLaplacian;
import org.openpnp.vision.pipeline.stages.DetectEdgesRobertsCross;
import org.openpnp.vision.pipeline.stages.DetectFixedCirclesHough;
import org.openpnp.vision.pipeline.stages.DetectLinesHough;
import org.openpnp.vision.pipeline.stages.DetectRectangleHough;
import org.openpnp.vision.pipeline.stages.DetectRectlinearSymmetry;
import org.openpnp.vision.pipeline.stages.DilateModel;
import org.openpnp.vision.pipeline.stages.DrawCircles;
import org.openpnp.vision.pipeline.stages.DrawContours;
import org.openpnp.vision.pipeline.stages.DrawEllipses;
import org.openpnp.vision.pipeline.stages.DrawImageCenter;
import org.openpnp.vision.pipeline.stages.DrawKeyPoints;
import org.openpnp.vision.pipeline.stages.DrawRotatedRects;
import org.openpnp.vision.pipeline.stages.DrawTemplateMatches;
import org.openpnp.vision.pipeline.stages.FilterContours;
import org.openpnp.vision.pipeline.stages.FilterRects;
import org.openpnp.vision.pipeline.stages.FindContours;
import org.openpnp.vision.pipeline.stages.FitEllipseContours;
import org.openpnp.vision.pipeline.stages.GrabCut;
import org.openpnp.vision.pipeline.stages.HistogramEqualize;
import org.openpnp.vision.pipeline.stages.HistogramEqualizeAdaptive;
import org.openpnp.vision.pipeline.stages.ImageCapture;
import org.openpnp.vision.pipeline.stages.ImageRead;
import org.openpnp.vision.pipeline.stages.ImageRecall;
import org.openpnp.vision.pipeline.stages.ImageWrite;
import org.openpnp.vision.pipeline.stages.ImageWriteDebug;
import org.openpnp.vision.pipeline.stages.MaskCircle;
import org.openpnp.vision.pipeline.stages.MaskHsv;
import org.openpnp.vision.pipeline.stages.MaskModel;
import org.openpnp.vision.pipeline.stages.MaskPolygon;
import org.openpnp.vision.pipeline.stages.MaskRectangle;
import org.openpnp.vision.pipeline.stages.MatchPartTemplate;
import org.openpnp.vision.pipeline.stages.MatchPartsTemplate;
import org.openpnp.vision.pipeline.stages.MatchTemplate;
import org.openpnp.vision.pipeline.stages.MinAreaRect;
import org.openpnp.vision.pipeline.stages.MinAreaRectContours;
import org.openpnp.vision.pipeline.stages.MinEnclosingCircle;
import org.openpnp.vision.pipeline.stages.Normalize;
import org.openpnp.vision.pipeline.stages.CapFindStage;
import org.openpnp.vision.pipeline.stages.OrientRotatedRects;
import org.openpnp.vision.pipeline.stages.ParameterBool;
import org.openpnp.vision.pipeline.stages.ParameterNumeric;
import org.openpnp.vision.pipeline.stages.ReadModelProperty;
import org.openpnp.vision.pipeline.stages.ReadPartTemplateImage;
import org.openpnp.vision.pipeline.stages.Rotate;
import org.openpnp.vision.pipeline.stages.ScriptRun;
import org.openpnp.vision.pipeline.stages.SelectSingleRect;
import org.openpnp.vision.pipeline.stages.SetColor;
import org.openpnp.vision.pipeline.stages.SimpleBlobDetector;
import org.openpnp.vision.pipeline.stages.SimpleOcr;
import org.openpnp.vision.pipeline.stages.SizeCheck;
import org.openpnp.vision.pipeline.stages.Threshold;
import org.openpnp.vision.pipeline.stages.ThresholdAdaptive;
import org.openpnp.vision.pipeline.stages.WritePartTemplateImage;
import org.pmw.tinylog.Logger;

/**
 * A JPanel based component for editing a CvPipeline. Allows the user to add and remove stages,
 * modify properties of each stage, see the image and model results from each stage and export and
 * import the pipeline from the clipboard.
 * 
 * The static method #registerStageClass can be used to register a CvStage implementation with this
 * component, allowing the user to select the implementation from the list when creating a new
 * stage.
 * 
 * The core CvStage classes are automatically registered during startup.
 */
@SuppressWarnings("serial")
public class CvPipelineEditor extends JPanel {
    static {
        stageClasses = new HashSet<>();
        // Parameter stages.
        registerStageClass(ParameterNumeric.class);
        registerStageClass(ParameterBool.class);
        // Vision stages.
        registerStageClass(BlurMedian.class);
        registerStageClass(BlurGaussian.class);
        registerStageClass(ClosestModel.class);
        registerStageClass(Add.class);
        registerStageClass(ComposeResult.class);
        registerStageClass(ConvertColor.class);
        registerStageClass(ConvertModelToPoints.class);
        registerStageClass(ConvertModelToKeyPoints.class);
        registerStageClass(CreateFootprintTemplateImage.class);
        registerStageClass(CreateModelTemplateImage.class);
        registerStageClass(CreateShapeTemplateImage.class);
        registerStageClass(DetectCirclesHough.class);
        registerStageClass(DetectLinesHough.class);
        registerStageClass(DetectRectangleHough.class);
        registerStageClass(DetectEdgesCanny.class);
        registerStageClass(DetectEdgesRobertsCross.class);
        registerStageClass(DetectEdgesLaplacian.class);
        registerStageClass(DetectFixedCirclesHough.class);
        registerStageClass(DetectCircularSymmetry.class);
        registerStageClass(DetectRectlinearSymmetry.class);
        registerStageClass(DilateModel.class);
        registerStageClass(DrawCircles.class);
        registerStageClass(DrawContours.class);
        registerStageClass(DrawImageCenter.class);
        registerStageClass(DrawKeyPoints.class);
        registerStageClass(DrawRotatedRects.class);
        registerStageClass(DrawEllipses.class);
        registerStageClass(DrawTemplateMatches.class);
        registerStageClass(FilterContours.class);
        registerStageClass(FilterRects.class);
        registerStageClass(FindContours.class);
        registerStageClass(GrabCut.class);
        registerStageClass(HistogramEqualize.class);
        registerStageClass(HistogramEqualizeAdaptive.class);
        registerStageClass(ImageCapture.class);
        registerStageClass(ImageRead.class);
        registerStageClass(ImageRecall.class);
        registerStageClass(ImageWrite.class);
        registerStageClass(ImageWriteDebug.class);
        registerStageClass(MaskCircle.class);
        registerStageClass(MaskHsv.class);
        registerStageClass(MaskModel.class);
        registerStageClass(MaskPolygon.class);
        registerStageClass(MaskRectangle.class);
        registerStageClass(MatchTemplate.class);
        registerStageClass(MatchPartTemplate.class);
        registerStageClass(MatchPartsTemplate.class);
        registerStageClass(MinAreaRect.class);
        registerStageClass(MinAreaRectContours.class);
        registerStageClass(MinEnclosingCircle.class);
        registerStageClass(FitEllipseContours.class);
        registerStageClass(Normalize.class);
        registerStageClass(CapFindStage.class);
        registerStageClass(OrientRotatedRects.class);
        registerStageClass(ReadModelProperty.class);
        registerStageClass(ReadPartTemplateImage.class);
        registerStageClass(Rotate.class);
        registerStageClass(SelectSingleRect.class);
        registerStageClass(SetColor.class);
        registerStageClass(ScriptRun.class);
        registerStageClass(SimpleBlobDetector.class);
        registerStageClass(SizeCheck.class);
        registerStageClass(Threshold.class);
        registerStageClass(ThresholdAdaptive.class);
        registerStageClass(WritePartTemplateImage.class);
        registerStageClass(ActuatorWrite.class);
        registerStageClass(AffineWarp.class);
        registerStageClass(AffineUnwarp.class);
        registerStageClass(SimpleOcr.class);
        
    }

    private final static Set<Class<? extends CvStage>> stageClasses;

    private final CvPipeline pipeline;
    private PipelinePanel pipelinePanel;
    private ResultsPanel resultsPanel;
    
    private String originalVersion = "";

    public CvPipelineEditor(CvPipeline pipeline) {
        this.pipeline = pipeline;
        try {
            originalVersion = pipeline.toXmlString();
        }
        catch (Exception e1) {
            // Do nothing
        }
        
        setLayout(new BorderLayout(0, 0));

        JSplitPane inputAndOutputSplitPane = new JSplitPane();
        inputAndOutputSplitPane.setContinuousLayout(true);
        add(inputAndOutputSplitPane, BorderLayout.CENTER);

        resultsPanel = new ResultsPanel(this);
        inputAndOutputSplitPane.setRightComponent(resultsPanel);
        pipelinePanel = new PipelinePanel(this);
        inputAndOutputSplitPane.setLeftComponent(pipelinePanel);
        
        addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                inputAndOutputSplitPane.setDividerLocation(0.25);
            }
        });
        
        process();
    }
    
    public void initializeFocus() {
        pipelinePanel.initializeFocus();    	
    }
    
    public CvPipeline getPipeline() {
        return pipeline;
    }

    public void process() {
        UiUtils.messageBoxOnException(() -> getPipeline().process());
        resultsPanel.refresh();
    }

    public void stageSelected(CvStage stage) {
        resultsPanel.setSelectedStage(stage);
    }

    public boolean isDirty( ) {
        String editedVersion = "";
        try {
            editedVersion = pipeline.toXmlString();
        }
        catch (Exception e) {
            // Do nothing
            Logger.warn(e);
        }
        return !editedVersion.equals(originalVersion);
    }
    
    public void undoEdits() {
        try {
            pipeline.fromXmlString(originalVersion);
        }
        catch (Exception e) {
            // Do nothing
        }
    }
    
    public static Set<Class<? extends CvStage>> getStageClasses() {
        return Collections.unmodifiableSet(stageClasses);
    }

    public static void registerStageClass(Class<? extends CvStage> cls) {
        stageClasses.add(cls);
    }
}
