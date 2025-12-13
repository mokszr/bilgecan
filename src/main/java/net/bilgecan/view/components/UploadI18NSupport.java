package net.bilgecan.view.components;

import net.bilgecan.service.TranslationService;
import com.vaadin.flow.component.upload.UploadI18N;

import java.util.Arrays;

public class UploadI18NSupport extends UploadI18N {

    public UploadI18NSupport(TranslationService translationService) {
        setDropFiles(new DropFiles()
                .setOne(translationService.t("uploadi18n.dropFilesOne"))
                .setMany(translationService.t("uploadi18n.dropFilesMany"))
        );
        setAddFiles(new AddFiles()
                .setOne(translationService.t("uploadi18n.addFilesOne"))
                .setMany(translationService.t("uploadi18n.addFilesMany"))
        );
        setError(new Error()
                .setTooManyFiles(translationService.t("uploadi18n.errorTooManyFiles"))
                .setFileIsTooBig(translationService.t("uploadi18n.errorFileIsTooBig"))
                .setIncorrectFileType(translationService.t("uploadi18n.errorIncorrectFileType"))
        );
        setUploading(new Uploading()
                .setStatus(new Uploading.Status()
                        .setConnecting(translationService.t("uploadi18n.uploading.status.connecting"))
                        .setStalled(translationService.t("uploadi18n.uploading.status.stalled"))
                        .setProcessing(translationService.t("uploadi18n.uploading.status.processing"))
                        .setHeld(translationService.t("uploadi18n.uploading.status.held"))
                )
                .setRemainingTime(new Uploading.RemainingTime()
                        .setPrefix(translationService.t("uploadi18n.uploading.remainingTime.prefix"))
                        .setUnknown(translationService.t("uploadi18n.uploading.remainingTime.unknown"))
                )
                .setError(new Uploading.Error()
                        .setServerUnavailable(translationService.t("uploadi18n.uploading.error.serverUnavailable"))
                        .setUnexpectedServerError(translationService.t("uploadi18n.uploading.error.unexpectedServerError"))
                        .setForbidden(translationService.t("uploadi18n.uploading.error.forbidden"))
                )
        );
        setUnits(new Units().setSize(Arrays.asList("B", "kB", "MB", "GB", "TB",
                "PB", "EB", "ZB", "YB")));
    }
}
