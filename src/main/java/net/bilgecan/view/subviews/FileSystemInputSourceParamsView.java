package net.bilgecan.view.subviews;

import net.bilgecan.dto.InputSourceDto;
import net.bilgecan.pipeline.fileprocessing.FileSystemInputFileProvider;
import net.bilgecan.pipeline.fileprocessing.InputSourceResolverService;
import net.bilgecan.service.TranslationService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.MimeTypeUtils;

import java.util.List;
import java.util.regex.Pattern;

public class FileSystemInputSourceParamsView extends VerticalLayout {

    private final TextField fileNamePatternField;
    private final ComboBox<String> directoryField;
    private final ComboBox<String> mimeTypeComboBox;
    private final InputSourceResolverService inputSourceResolverService;
    private TranslationService translations;
    private Binder<InputSourceDto> binder;
    private InputSourceDto inputSourceDto;

    public FileSystemInputSourceParamsView(InputSourceResolverService inputSourceResolverService, TranslationService translations) {
        this.inputSourceResolverService = inputSourceResolverService;
        this.translations = translations;
        setSizeFull();
        setPadding(false);

        binder = new Binder<>(InputSourceDto.class);

        fileNamePatternField = new TextField(translations.t("fileSystemInputSourceParams.fileNamePattern"));
        fileNamePatternField.setWidthFull();
        fileNamePatternField.setPlaceholder(translations.t("fileSystemInputSourceParams.fileNamePatternPlaceholder"));

        binder.forField(fileNamePatternField)
                .withValidator(value -> {
                    try {
                        Pattern.compile(value); // ".*\\.csv" e.g. all .csv files
                        return StringUtils.isNotBlank(value);
                    } catch (Exception e) {
                        return false;
                    }
                }, translations.t("fileSystemInputSourceParams.fileNamePatternInvalid"))
                .bind(this::getFileNamePattern,
                        this::setFileNamePattern);

        directoryField = new ComboBox<>(translations.t("fileSystemInputSourceParams.directory"));
        directoryField.setWidthFull();

        List<String> directories = inputSourceResolverService.getFileSystemInputDirectories();

        directoryField.setItems(directories);

        binder.forField(directoryField)
                .withValidator(StringUtils::isNotBlank, translations.t("validation.fieldCannotBeBlank", translations.t("fileSystemInputSourceParams.directory")))
                .bind(this::getDirectory,
                        this::setDirectory);

        mimeTypeComboBox = new ComboBox<>(translations.t("fileSystemInputSourceParams.mimeType"));
        mimeTypeComboBox.setWidthFull();

        mimeTypeComboBox.setItems(MimeTypeUtils.IMAGE_JPEG_VALUE,
                MimeTypeUtils.IMAGE_PNG_VALUE,
                MimeTypeUtils.IMAGE_GIF_VALUE,
                MimeTypeUtils.TEXT_PLAIN_VALUE,
                MimeTypeUtils.TEXT_HTML_VALUE,
                MimeTypeUtils.TEXT_XML_VALUE,
                MimeTypeUtils.APPLICATION_GRAPHQL_VALUE,
                MimeTypeUtils.APPLICATION_JSON_VALUE,
                MimeTypeUtils.APPLICATION_XML_VALUE,
                MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE,
                MimeTypeUtils.ALL_VALUE);

        binder.forField(mimeTypeComboBox)
                .withValidator(StringUtils::isNotBlank, translations.t("validation.fieldCannotBeBlank", translations.t("fileSystemInputSourceParams.mimeType")))
                .bind(this::getMimeType,
                        this::setMimeType);

        add(fileNamePatternField);
        add(directoryField);
        add(mimeTypeComboBox);

    }

    private String getFileNamePattern(InputSourceDto dto) {
        return dto.getConfigs().get(FileSystemInputFileProvider.FILE_NAME_PATTERN);
    }

    private void setFileNamePattern(InputSourceDto dto, String fileNamePattern) {
        dto.getConfigs().put(FileSystemInputFileProvider.FILE_NAME_PATTERN, fileNamePattern);
    }

    private String getDirectory(InputSourceDto dto) {
        return dto.getConfigs().get(FileSystemInputFileProvider.DIRECTORY_NAME);
    }

    private void setDirectory(InputSourceDto dto, String directory) {
        dto.getConfigs().put(FileSystemInputFileProvider.DIRECTORY_NAME, directory);
    }

    private String getMimeType(InputSourceDto dto) {
        return dto.getConfigs().get(FileSystemInputFileProvider.MIME_TYPE);
    }

    private void setMimeType(InputSourceDto dto, String mimeType) {
        dto.getConfigs().put(FileSystemInputFileProvider.MIME_TYPE, mimeType);
    }

    public void writeBean() throws ValidationException {
        binder.writeBean(inputSourceDto);
    }

    public void readBean(InputSourceDto inputSourceDto) {
        this.inputSourceDto = inputSourceDto;
        binder.readBean(inputSourceDto);
    }

}
