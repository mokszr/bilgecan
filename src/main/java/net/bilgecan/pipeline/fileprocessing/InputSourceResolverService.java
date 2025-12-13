package net.bilgecan.pipeline.fileprocessing;

import net.bilgecan.entity.InputSource;
import net.bilgecan.entity.InputSourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class InputSourceResolverService {

    private FileSystemInputFileProvider fileSystemInputFileProvider;

    public List<String> getFiles(InputSource inputSource) {

        InputSourceType type = inputSource.getType();
        if(type.equals(InputSourceType.FILE_SYSTEM)) {
            return fileSystemInputFileProvider.getFiles(inputSource);
        }

        return Collections.emptyList();
    }

    @Autowired
    public void setFileSystemInputFileProvider(FileSystemInputFileProvider fileSystemInputFileProvider) {
        this.fileSystemInputFileProvider = fileSystemInputFileProvider;
    }

    public List<String> getFileSystemInputDirectories() {
       return fileSystemInputFileProvider.getInputDirectories();
    }
}
