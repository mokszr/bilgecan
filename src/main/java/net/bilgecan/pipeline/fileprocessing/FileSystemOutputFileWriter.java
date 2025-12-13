package net.bilgecan.pipeline.fileprocessing;

import net.bilgecan.dto.AITaskRunDto;
import net.bilgecan.entity.OutputTarget;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FileSystemOutputFileWriter implements OutputFileWriter {

    public static final String DIRECTORY_NAME = "DIRECTORY_NAME";
    public static final String ARCHIVE_DIRECTORY_NAME = "ARCHIVE_DIRECTORY_NAME";
    public static final String MOVE_FILE_TO_ARCHIVE = "MOVE_FILE_TO_ARCHIVE";

    public static final String DEFAULT_OUTPUT_FILE_NAME = "ai_task_run_";
    private String rootOutputFileDirectoryPath;
    private String rootArchiveFileDirectoryPath;
    private String rootInputFileDirectoryPath;

    @Override
    public void writeOutput(AITaskRunDto aiTaskRunDto, OutputTarget outputTarget) {

        String directoryName = outputTarget.getConfigs().get(DIRECTORY_NAME);
        String archiveDirectoryName = outputTarget.getConfigs().get(ARCHIVE_DIRECTORY_NAME);
        boolean moveFileToArchive = Boolean.parseBoolean(outputTarget.getConfigs().get(MOVE_FILE_TO_ARCHIVE));

        String outputTextAi = aiTaskRunDto.getAiResponseDetails().getOutputTextAi() + System.lineSeparator();

        Path outputFilePath = Paths.get(rootOutputFileDirectoryPath + File.separator + directoryName + File.separator + DEFAULT_OUTPUT_FILE_NAME + aiTaskRunDto.getId() + ".txt");
        try {
            Files.write(outputFilePath, outputTextAi.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);

            if (moveFileToArchive) {
                moveToArchive(aiTaskRunDto, archiveDirectoryName);
            }
        } catch (IOException e) {
            //TODO improve this exception handling
            throw new RuntimeException(e);
        }

    }

    private void moveToArchive(AITaskRunDto aiTaskRunDto, String archiveDirectoryName) throws IOException {
        if (StringUtils.isBlank(aiTaskRunDto.getInputFilePath())) {
            return;
        }
        Path source = Paths.get(rootInputFileDirectoryPath + File.separator + aiTaskRunDto.getInputFilePath());
        Path targetDir = Paths.get(rootArchiveFileDirectoryPath + File.separator + archiveDirectoryName);
        Files.move(source, targetDir.resolve(source.getFileName()),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE);
    }


    public List<String> getOutputDirectories() {
        try {
            return Files.list(Paths.get(rootOutputFileDirectoryPath))
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getArchiveDirectories() {
        try {
            return Files.list(Paths.get(rootArchiveFileDirectoryPath))
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Value("${bilgecan.rootOutputFileDirectoryPath}")
    public void setRootOutputFileDirectoryPath(String rootOutputFileDirectoryPath) {
        this.rootOutputFileDirectoryPath = rootOutputFileDirectoryPath;
    }

    @Value("${bilgecan.rootArchiveFileDirectoryPath}")
    public void setRootArchiveFileDirectoryPath(String rootArchiveFileDirectoryPath) {
        this.rootArchiveFileDirectoryPath = rootArchiveFileDirectoryPath;
    }

    @Value("${bilgecan.rootInputFileDirectoryPath}")
    public void setRootInputFileDirectoryPath(String rootInputFileDirectoryPath) {
        this.rootInputFileDirectoryPath = rootInputFileDirectoryPath;
    }

    public String getRootArchiveFileDirectoryPath() {
        return rootArchiveFileDirectoryPath;
    }
}
