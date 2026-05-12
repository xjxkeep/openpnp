package org.openpnp.machine.reference.feeder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.spi.Camera;
import org.openpnp.vision.pipeline.CvStage;

public class ReferenceStripFeederTest {
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
}
