package org.openpnp.machine.reference.feeder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openpnp.machine.reference.feeder.ReferenceStripFeeder.TapeOrientation;
import org.openpnp.model.Configuration;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.spi.Camera;
import org.openpnp.vision.pipeline.CvStage;

import com.google.common.io.Files;

public class ReferenceStripFeederTest {
    @BeforeEach
    public void setUp() {
        File workingDirectory = Files.createTempDir();
        Configuration.initialize(workingDirectory);
    }

    @Test
    public void closestHoleUsesExpectedLocationInsteadOfCameraCenter() {
        Camera camera = mock(Camera.class);
        when(camera.getLocation()).thenReturn(new Location(LengthUnit.Millimeters, 5, 0, 0, 0));
        when(camera.getUnitsPerPixelAtZ()).thenReturn(new Location(LengthUnit.Millimeters, 1, 1, 0, 0));
        when(camera.getWidth()).thenReturn(10);
        when(camera.getHeight()).thenReturn(10);

        Location expectedLocation = new Location(LengthUnit.Millimeters, 3, 0, 0, 0);
        List<CvStage.Result.Circle> results = new ArrayList<>();
        results.add(new CvStage.Result.Circle(5, 5, 1));
        results.add(new CvStage.Result.Circle(3, 5, 1));

        Location closestLocation = ReferenceStripFeeder.getClosestHoleLocation(camera,
                expectedLocation, results);

        assertEquals(expectedLocation, closestLocation);
    }

    @Test
    public void tapeOrientationMirrorsPickLocationAroundReferenceHole() throws Exception {
        assertPickLocation(TapeOrientation.ReferenceHoleAfterPartRight, -3.5, -2.0);
        assertPickLocation(TapeOrientation.ReferenceHoleAfterPartLeft, 3.5, -2.0);
        assertPickLocation(TapeOrientation.ReferenceHoleBeforePartRight, -3.5, 2.0);
        assertPickLocation(TapeOrientation.ReferenceHoleBeforePartLeft, 3.5, 2.0);
    }

    private static void assertPickLocation(TapeOrientation tapeOrientation, double x, double y)
            throws Exception {
        ReferenceStripFeeder feeder = new ReferenceStripFeeder();
        feeder.setReferenceHoleLocation(new Location(LengthUnit.Millimeters, 0, 0, 0, 0));
        feeder.setLastHoleLocation(new Location(LengthUnit.Millimeters, 0, 4, 0, 0));
        feeder.setTapeOrientation(tapeOrientation);
        feeder.setFeedCount(1);

        Location pickLocation = feeder.getPickLocation();

        assertEquals(x, pickLocation.getX(), 0.0001);
        assertEquals(y, pickLocation.getY(), 0.0001);
    }
}
