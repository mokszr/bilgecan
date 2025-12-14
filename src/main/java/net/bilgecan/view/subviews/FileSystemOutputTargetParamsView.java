package net.bilgecan.view.subviews;

import net.bilgecan.dto.OutputTargetDto;
import net.bilgecan.pipeline.fileprocessing.FileSystemOutputFileWriter;
import net.bilgecan.pipeline.fileprocessing.OutputWriterService;
import net.bilgecan.service.TranslationService;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class FileSystemOutputTargetParamsView extends VerticalLayout {

    private final ComboBox<String> outputDirectoryField;
    private ComboBox<String> archiveDirectoryComboBox = null;
    private final OutputWriterService outputWriterService;
    private TranslationService translations;
    private Binder<OutputTargetDto> binder;
    private OutputTargetDto outputTargetDto;

    public FileSystemOutputTargetParamsView(OutputWriterService outputWriterService, TranslationService translations) {
        this.outputWriterService = outputWriterService;
        this.translations = translations;
        setSizeFull();
        setPadding(false);

        binder = new Binder<>(OutputTargetDto.class);

        outputDirectoryField = new ComboBox<>(translations.t("fileSystemOutputTargetParams.outputDirectory"));
        outputDirectoryField.setWidthFull();

        List<String> directories = outputWriterService.getFileSystemOutputDirectories();

        outputDirectoryField.setItems(directories);

        binder.forField(outputDirectoryField)
                .withValidator(StringUtils::isNotBlank, translations.t("validation.fieldCannotBeBlank", translations.t("fileSystemOutputTargetParams.outputDirectory")))
                .bind(this::getOutputDirectory,
                        this::setOutputDirectory);

        Checkbox moveFileToArchive = new Checkbox(translations.t("fileSystemOutputTargetParams.moveFileToArchive"));
        binder.forField(moveFileToArchive)
                .bind(this::getMoveFileToArchive,
                        this::setMoveFileToArchive);

        moveFileToArchive.addValueChangeListener(event -> {
            Boolean moveToArchive = event.getValue();
            archiveDirectoryComboBox.setVisible(moveToArchive);
        });

        archiveDirectoryComboBox = new ComboBox<>(translations.t("fileSystemOutputTargetParams.archiveDirectory"));
        archiveDirectoryComboBox.setWidthFull();

        List<String> archiveDirectories = outputWriterService.getFileSystemArchiveDirectories();

        archiveDirectoryComboBox.setItems(archiveDirectories);

        binder.forField(archiveDirectoryComboBox)
                .withValidator(aPath -> {
                    if (moveFileToArchive.getValue()) {
                        return StringUtils.isNotBlank(aPath);
                    } else {
                        return true;
                    }
                }, translations.t("validation.fieldCannotBeBlank", translations.t("fileSystemOutputTargetParams.archiveDirectory")))
                .bind(this::getArchiveDirectory,
                        this::setArchiveDirectory);

        add(outputDirectoryField);
        add(moveFileToArchive);
        add(archiveDirectoryComboBox);

    }

    private String getOutputDirectory(OutputTargetDto dto) {
        return dto.getConfigs().get(FileSystemOutputFileWriter.DIRECTORY_NAME);
    }

    private void setOutputDirectory(OutputTargetDto dto, String directory) {
        dto.getConfigs().put(FileSystemOutputFileWriter.DIRECTORY_NAME, directory);
    }

    private Boolean getMoveFileToArchive(OutputTargetDto dto) {
        String moveFileToArchive = dto.getConfigs().get(FileSystemOutputFileWriter.MOVE_FILE_TO_ARCHIVE);
        //let default value true
        return moveFileToArchive == null ? true : Boolean.parseBoolean(moveFileToArchive);
    }

    private void setMoveFileToArchive(OutputTargetDto dto, boolean moveFileToArchive) {
        dto.getConfigs().put(FileSystemOutputFileWriter.MOVE_FILE_TO_ARCHIVE, Boolean.toString(moveFileToArchive));
    }

    private String getArchiveDirectory(OutputTargetDto dto) {
        return dto.getConfigs().get(FileSystemOutputFileWriter.ARCHIVE_DIRECTORY_NAME);
    }

    private void setArchiveDirectory(OutputTargetDto dto, String archiveDirectory) {
        dto.getConfigs().put(FileSystemOutputFileWriter.ARCHIVE_DIRECTORY_NAME, archiveDirectory);
    }

    public void writeBean() throws ValidationException {
        binder.writeBean(outputTargetDto);
    }

    public void readBean(OutputTargetDto outputTargetDto) {
        this.outputTargetDto = outputTargetDto;
        binder.readBean(outputTargetDto);

        archiveDirectoryComboBox.setVisible(getMoveFileToArchive(outputTargetDto));
    }

}
