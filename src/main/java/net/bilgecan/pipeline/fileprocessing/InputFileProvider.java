package net.bilgecan.pipeline.fileprocessing;

import net.bilgecan.entity.InputSource;

import java.util.List;

public interface InputFileProvider {

    List<String> getFiles(InputSource inputSource);
}
