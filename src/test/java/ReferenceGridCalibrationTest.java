import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.openpnp.machine.reference.camera.calibration.ReferenceGridCalibration;
import org.openpnp.machine.reference.camera.calibration.ReferenceGridCalibration.PixelTransform;
import org.openpnp.model.Length;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.model.Point;

public class ReferenceGridCalibrationTest {
    private static final double EPSILON = 0.000001;

    @Test
    public void interpolatesMotionCorrectionInsideGrid() throws Exception {
        ReferenceGridCalibration calibration = createTwoByTwoCalibration();

        Location node = calibration.getMotionCorrection(new Location(LengthUnit.Millimeters, 10, 10, 0, 0));
        assertEquals(1.0, node.getX(), EPSILON);
        assertEquals(0.0, node.getY(), EPSILON);

        Location center = calibration.getMotionCorrection(new Location(LengthUnit.Millimeters, 5, 5, 0, 0));
        assertEquals(0.5, center.getX(), EPSILON);
        assertEquals(0.5, center.getY(), EPSILON);
    }

    @Test
    public void returnsZeroCorrectionOutsideGrid() throws Exception {
        ReferenceGridCalibration calibration = createTwoByTwoCalibration();

        Location correction = calibration.getMotionCorrection(new Location(LengthUnit.Millimeters, 20, 20, 0, 0));
        assertEquals(0.0, correction.getX(), EPSILON);
        assertEquals(0.0, correction.getY(), EPSILON);
    }

    @Test
    public void usesLocalPixelTransformInsideGridAndFallbackOutside() throws Exception {
        ReferenceGridCalibration calibration = createTwoByTwoCalibration();
        Location fallback = new Location(LengthUnit.Millimeters, 0.1, 0.2, 0, 0);

        Location localOffset = calibration.getPixelOffsets(
                new Location(LengthUnit.Millimeters, 5, 5, 0, 0), fallback, 100, 50);
        assertEquals(1.0, localOffset.getX(), EPSILON);
        assertEquals(-1.0, localOffset.getY(), EPSILON);

        Point pixels = calibration.getLocationPixels(
                new Location(LengthUnit.Millimeters, 5, 5, 0, 0),
                new Location(LengthUnit.Millimeters, 6, 4, 0, 0), fallback);
        assertEquals(100.0, pixels.getX(), EPSILON);
        assertEquals(50.0, pixels.getY(), EPSILON);

        Location fallbackOffset = calibration.getPixelOffsets(
                new Location(LengthUnit.Millimeters, 20, 20, 0, 0), fallback, 100, 50);
        assertEquals(10.0, fallbackOffset.getX(), EPSILON);
        assertEquals(-10.0, fallbackOffset.getY(), EPSILON);
    }

    @Test
    public void invalidDataDoesNotEnableMapping() throws Exception {
        ReferenceGridCalibration calibration = new ReferenceGridCalibration();
        calibration.setEnabled(true);
        assertEquals(false, calibration.isMotionMappingValid());
        assertEquals(false, calibration.isPixelMappingValid());
    }

    private ReferenceGridCalibration createTwoByTwoCalibration() throws Exception {
        ReferenceGridCalibration calibration = new ReferenceGridCalibration();
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

        setPoint(calibration, 0, 0, 0, 0);
        setPoint(calibration, 0, 1, 1, 0);
        setPoint(calibration, 1, 0, 0, 1);
        setPoint(calibration, 1, 1, 1, 1);
        calibration.setEnabled(true);
        return calibration;
    }

    private void setPoint(ReferenceGridCalibration calibration, int row, int column,
            double correctionX, double correctionY) {
        Location nominal = calibration.getNominalLocation(row, column);
        Location measured = nominal.add(new Location(LengthUnit.Millimeters, correctionX, correctionY, 0, 0));
        calibration.setGridPoint(row, column, nominal, measured,
                new PixelTransform(0.01, 0.0, 0.0, -0.02));
    }
}
