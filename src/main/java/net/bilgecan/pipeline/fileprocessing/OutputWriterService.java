package net.bilgecan.pipeline.fileprocessing;

import net.bilgecan.dto.AITaskRunDto;
import net.bilgecan.entity.OutputTarget;
import net.bilgecan.entity.OutputTargetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OutputWriterService {

    private FileSystemOutputFileWriter fileSystemOutputFileWriter;

    public void writeOutput(AITaskRunDto aiTaskRunDto, OutputTarget outputTarget) {

        OutputTargetType type = outputTarget.getType();
        if(type.equals(OutputTargetType.FILE_SYSTEM)) {
            fileSystemOutputFileWriter.writeOutput(aiTaskRunDto, outputTarget);
        }
    }

    public List<String> getFileSystemOutputDirectories() {
        return fileSystemOutputFileWriter.getOutputDirectories();
    }

    public List<String> getFileSystemArchiveDirectories() {
        return fileSystemOutputFileWriter.getArchiveDirectories();
    }

    @Autowired
    public void setFileSystemOutputFileWriter(FileSystemOutputFileWriter fileSystemOutputFileWriter) {
        this.fileSystemOutputFileWriter = fileSystemOutputFileWriter;
    }
}
