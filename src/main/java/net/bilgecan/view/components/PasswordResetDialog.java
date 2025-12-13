package net.bilgecan.view.components;

import net.bilgecan.config.ConstantConfigs;
import net.bilgecan.service.TranslationService;
import net.bilgecan.support.NotificationSupport;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import org.apache.commons.lang3.StringUtils;

import java.util.function.BiConsumer;

public class PasswordResetDialog extends Dialog {

    private PasswordField oldPasswordField;
    private final PasswordField newPasswordField;
    private final PasswordField confirmPasswordField;

    public PasswordResetDialog(TranslationService translations, String username, boolean checkOldPassword, BiConsumer<String, String> onPasswordReset) {
        setHeaderTitle(translations.t("passwordReset.title"));

        Span usernameSpan = new Span(translations.t("general.username") + ": " + username);

        oldPasswordField = new PasswordField(translations.t("passwordReset.oldPassword"));
        oldPasswordField.setWidthFull();
        oldPasswordField.setVisible(checkOldPassword);

        newPasswordField = new PasswordField(translations.t("passwordReset.newPassword"));
        confirmPasswordField = new PasswordField(translations.t("passwordReset.confirmPassword"));
        newPasswordField.setWidthFull();
        confirmPasswordField.setWidthFull();

        newPasswordField.setRequired(true);
        newPasswordField.getElement().setAttribute("autocomplete", "new-password");
        newPasswordField.setPattern(ConstantConfigs.PASSWORD_REGEX);
        newPasswordField.setMinLength(ConstantConfigs.MIN_PASSWORD_LENGTH);
        newPasswordField.setMaxLength(ConstantConfigs.MAX_PASSWORD_LENGTH);

        newPasswordField.setI18n(new PasswordField.PasswordFieldI18n()
                .setRequiredErrorMessage(translations.t("general.required"))
                .setMinLengthErrorMessage(translations.t("signup.password.minimumLength"))
                .setMaxLengthErrorMessage(translations.t("signup.password.maximumLength"))
                .setPatternErrorMessage(translations.t("signup.password.patternErrorMessage")));
        newPasswordField.setHelperText(translations.t("signup.password.helperText"));

        VerticalLayout content = new VerticalLayout(usernameSpan, oldPasswordField, newPasswordField, confirmPasswordField);
        content.setPadding(false);
        content.setSpacing(true);
        content.setWidth("400px");
        add(content);

        Button cancel = new Button(translations.t("button.cancel"), e -> close());

        Button reset = new Button(translations.t("button.reset"), e -> {
            String newPassword = newPasswordField.getValue();
            String confirm = confirmPasswordField.getValue();

            if (newPassword == null || newPassword.isBlank()
                    || confirm == null || confirm.isBlank()) {

                NotificationSupport.showError(translations.t("passwordReset.emptyFieldsError"));
                return;
            }

            if (checkOldPassword && StringUtils.isBlank(oldPasswordField.getValue())) {
                NotificationSupport.showError(translations.t("passwordReset.emptyFieldsError"));
                return;
            }

            if (!newPassword.equals(confirm)) {
                NotificationSupport.showError(translations.t("passwordReset.passwordsDontMatch"));
                return;
            }

            boolean isValid = true;
            // Validate password
            if (!ConstantConfigs.PASSWORD_POLICY.matcher(newPasswordField.getValue()).matches()) {
                newPasswordField.setInvalid(true);
                isValid = false;
            } else {
                newPasswordField.setInvalid(false);
            }

            if (isValid) {
                // Call your business logic
                onPasswordReset.accept(oldPasswordField.getValue(), newPassword);
            } else {
                NotificationSupport.showError(translations.t("passwordReset.invalidPassword"));
            }

        });
        reset.setIcon(VaadinIcon.KEY.create());
        reset.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout footer = new HorizontalLayout(cancel, reset);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footer.setSpacing(true);

        getFooter().add(footer);
    }
}
