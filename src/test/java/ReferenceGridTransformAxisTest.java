import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.Action;
import javax.swing.Icon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openpnp.model.AxesLocation;
import org.openpnp.model.Configuration;
import org.openpnp.model.Length;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.machine.reference.ReferenceMachine;
import org.openpnp.machine.reference.axis.ReferenceGridTransformAxis;
import org.openpnp.machine.reference.axis.ReferenceVirtualAxis;
import org.openpnp.machine.reference.camera.ReferenceCamera;
import org.openpnp.machine.reference.camera.calibration.ReferenceGridCalibration;
import org.openpnp.machine.reference.camera.calibration.ReferenceGridCalibration.PixelTransform;
import org.openpnp.gui.support.Wizard;
import org.openpnp.spi.Axis;
import org.openpnp.spi.Locatable.LocationOption;
import org.openpnp.spi.PropertySheetHolder;

import com.google.common.io.Files;

public class ReferenceGridTransformAxisTest {
    private static final double EPSILON = 0.001;

    private ReferenceMachine machine;
    private ReferenceVirtualAxis rawX;
    private ReferenceVirtualAxis rawY;
    private ReferenceGridTransformAxis gridX;
    private ReferenceGridTransformAxis gridY;

    @BeforeEach
    public void before() throws Exception {
        File workingDirectory = Files.createTempDir();
        workingDirectory = new File(workingDirectory, ".openpnp");
        Configuration.initialize(workingDirectory);
        machine = new ReferenceMachine();
        Configuration.get().setMachine(machine);

        rawX = new ReferenceVirtualAxis(Axis.Type.X);
        rawX.setName("Raw X");
        rawY = new ReferenceVirtualAxis(Axis.Type.Y);
        rawY.setName("Raw Y");
        machine.addAxis(rawX);
        machine.addAxis(rawY);

        TestCamera camera = new TestCamera();
        camera.setName("Grid Camera");
        configureConstantCorrection(camera.getGridCalibration(), 1.0, -0.5);

        gridX = createGridAxis(camera, Axis.Type.X);
        gridY = createGridAxis(camera, Axis.Type.Y);
        machine.addAxis(gridX);
        machine.addAxis(gridY);
    }

    @Test
    public void toRawAppliesGridCorrection() throws Exception {
        AxesLocation transformed = new AxesLocation(gridX, 5.0)
                .put(new AxesLocation(gridY, 5.0));

        AxesLocation raw = gridX.toRaw(transformed);

        assertEquals(6.0, raw.getCoordinate(rawX), EPSILON);
        assertEquals(4.5, raw.getCoordinate(rawY), EPSILON);
    }

    @Test
    public void toRawCanSuppressGridCorrection() throws Exception {
        AxesLocation transformed = new AxesLocation(gridX, 5.0)
                .put(new AxesLocation(gridY, 5.0));

        AxesLocation raw = gridX.toRaw(transformed, LocationOption.SuppressStaticCompensation);

        assertEquals(5.0, raw.getCoordinate(rawX), EPSILON);
        assertEquals(5.0, raw.getCoordinate(rawY), EPSILON);
    }

    @Test
    public void toTransformedApproximatesInverseGridCorrection() {
        AxesLocation raw = new AxesLocation(rawX, 6.0)
                .put(new AxesLocation(rawY, 4.5));

        AxesLocation transformed = gridX.toTransformed(raw);

        assertEquals(5.0, transformed.getCoordinate(gridX), EPSILON);
        assertEquals(5.0, transformed.getCoordinate(gridY), EPSILON);
    }

    private ReferenceGridTransformAxis createGridAxis(TestCamera camera, Axis.Type type) {
        ReferenceGridTransformAxis axis = new ReferenceGridTransformAxis();
        axis.setName("Grid " + type);
        axis.setType(type);
        axis.setInputAxisX(rawX);
        axis.setInputAxisY(rawY);
        axis.setCalibrationCamera(camera);
        axis.setCompensation(true);
        return axis;
    }

    private void configureConstantCorrection(ReferenceGridCalibration calibration,
            double correctionX, double correctionY) throws Exception {
        calibration.setPitch(new Length(10, LengthUnit.Millimeters));
        calibration.setCorner(ReferenceGridCalibration.CORNER_LEFT_TOP,
                new Location(LengthUnit.Millimeters, 0, 10, 0, 0));
        calibration.setCorner(ReferenceGridCalibration.CORNER_RIGHT_TOP,
                new Location(LengthUnit.Millimeters, 10, 10, 0, 0));
        calibration.setCorner(ReferenceGridCalibration.CORNER_RIGHT_BOTTOM,
                new Location(LengthUnit.Millimeters, 10, 0, 0, 0));
        calibration.setCorner(ReferenceGridCalibration.CORNER_LEFT_BOTTOM,
                new Location(LengthUnit.Millimeters, 0, 0, 0, 0));
        calibration.inferGrid();
        for (int row = 0; row < calibration.getRows(); row++) {
            for (int column = 0; column < calibration.getColumns(); column++) {
                Location nominal = calibration.getNominalLocation(row, column);
                Location measured = nominal.add(new Location(LengthUnit.Millimeters, correctionX, correctionY, 0, 0));
                calibration.setGridPoint(row, column, nominal, measured,
                        new PixelTransform(0.01, 0.0, 0.0, -0.01));
            }
        }
        calibration.setEnabled(true);
    }

    private static class TestCamera extends ReferenceCamera {
        @Override
        protected BufferedImage internalCapture() {
            return null;
        }

        @Override
        public Wizard getConfigurationWizard() {
            return null;
        }

        @Override
        public String getPropertySheetHolderTitle() {
            return getName();
        }

        @Override
        public PropertySheetHolder[] getChildPropertySheetHolders() {
            return null;
        }

        @Override
        public PropertySheet[] getPropertySheets() {
            return null;
        }

        @Override
        public Action[] getPropertySheetHolderActions() {
            return null;
        }

        @Override
        public Icon getPropertySheetHolderIcon() {
            return null;
        }
    }
}
