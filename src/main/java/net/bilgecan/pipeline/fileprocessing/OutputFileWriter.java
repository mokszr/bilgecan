package net.bilgecan.pipeline.fileprocessing;

import net.bilgecan.dto.AITaskRunDto;
import net.bilgecan.entity.OutputTarget;

public interface OutputFileWriter {

    void writeOutput(AITaskRunDto aiTaskRunDto, OutputTarget outputTarget);
}
