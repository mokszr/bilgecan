package net.bilgecan.config;

import net.bilgecan.exception.AppLevelValidationException;
import net.bilgecan.service.TranslationService;
import net.bilgecan.support.NotificationSupport;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authorization.AuthorizationDeniedException;

public class CustomErrorHandler implements ErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomErrorHandler.class);

    private TranslationService translations;

    public CustomErrorHandler(TranslationService translations) {
        this.translations = translations;
    }

    @Override
    public void error(ErrorEvent errorEvent) {
        Throwable throwable = errorEvent.getThrowable();
        logger.error("Something wrong happened", throwable);
        if (throwable instanceof AppLevelValidationException) {
            handleAppLevelValidationException((AppLevelValidationException) throwable);
            return;
        }

        if(throwable instanceof DataIntegrityViolationException) {
            handleCannotDeleteException((DataIntegrityViolationException) throwable);
            return;
        }

        if(throwable instanceof AuthorizationDeniedException) {
            handleAccessDeniedException((AuthorizationDeniedException) throwable);
            return;
        }

        if (UI.getCurrent() != null) {
            UI.getCurrent().access(() -> {
                NotificationSupport.showError(translations.t("error.general"), 3000);
            });
        }
    }

    private void handleAccessDeniedException(AuthorizationDeniedException throwable) {
        if (UI.getCurrent() != null) {
            UI.getCurrent().access(() -> {
                NotificationSupport.showError(translations.t("error.accessDenied"), 3000);
            });
        }
    }

    private void handleCannotDeleteException(DataIntegrityViolationException dataIntegrityViolationException) {
        if (UI.getCurrent() != null) {
            UI.getCurrent().access(() -> {
                NotificationSupport.showError(translations.t("error.cannotDeleteBecauseOfForeignKey"), 3000);
            });
        }
    }

    private void handleAppLevelValidationException(AppLevelValidationException validationException) {
        if (UI.getCurrent() != null) {
            UI.getCurrent().access(() -> {
                if (validationException.getPosition() != null) {
                    NotificationSupport.showError(validationException.getMessage(), 3000, validationException.getPosition());
                } else {
                    NotificationSupport.showError(validationException.getMessage(), 3000);
                }
            });
        }
    }
}