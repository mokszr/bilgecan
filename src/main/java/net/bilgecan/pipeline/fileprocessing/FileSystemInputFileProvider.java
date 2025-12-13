package net.bilgecan.pipeline.fileprocessing;

import net.bilgecan.entity.InputSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class FileSystemInputFileProvider implements InputFileProvider {

    public static final String FILE_NAME_PATTERN = "FILE_NAME_PATTERN";
    public static final String DIRECTORY_NAME = "DIRECTORY_NAME";
    public static final String MIME_TYPE = "MIME_TYPE";

    private String rootInputFileDirectoryPath;
    private final List<String> blackListedFileNames = Arrays.asList(".DS_Store", ".AppleDouble",
            "Thumbs.db",
            "Desktop.ini",
            ".git",
            ".gitignore",
            ".gitattributes",
            ".gitmodules",
            ".idea",
            ".classpath",
            ".project",
            ".settings",
            ".vscode");

    @Override
    public List<String> getFiles(InputSource inputSource) {

        String fileNamePattern = inputSource.getConfigs().get(FILE_NAME_PATTERN);
        String directoryName = inputSource.getConfigs().get(DIRECTORY_NAME);

        Path dir = Paths.get(rootInputFileDirectoryPath + File.separator + directoryName);
        Pattern pattern = Pattern.compile(fileNamePattern); // ".*\\.csv" e.g. all .csv files

        try (var stream = Files.list(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> pattern.matcher(path.getFileName().toString()).matches() && !blackListedFileNames.contains(path.getFileName().toString()))
                    .map(p -> directoryName + File.separator + p.getFileName())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            //TODO improve here
            throw new RuntimeException(e);
        }
    }


    public List<String> getInputDirectories() {
        try {
            return Files.list(Paths.get(rootInputFileDirectoryPath))
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Value("${bilgecan.rootInputFileDirectoryPath}")
    public void setRootInputFileDirectoryPath(String rootInputFileDirectoryPath) {
        this.rootInputFileDirectoryPath = rootInputFileDirectoryPath;
    }

    public String getRootInputFileDirectoryPath() {
        return rootInputFileDirectoryPath;
    }
}
