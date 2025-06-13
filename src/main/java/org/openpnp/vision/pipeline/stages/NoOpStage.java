package org.openpnp.vision.pipeline.stages;

import org.openpnp.vision.pipeline.CvPipeline;
import org.openpnp.vision.pipeline.CvStage;

public class NoOpStage extends CvStage {
    @Override
    public Result process(CvPipeline pipeline) throws Exception {
        // 原样返回当前 working image/model/colorSpace
        return new Result(
            pipeline.getWorkingImage(),
            pipeline.getWorkingColorSpace(),
            pipeline.getWorkingModel()
        );
    }

    @Override
    public String getName() {
        return "NoOpStage";
    }
} 