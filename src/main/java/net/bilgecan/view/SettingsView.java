package net.bilgecan.view;

import net.bilgecan.entity.security.User;
import net.bilgecan.service.SecurityService;
import net.bilgecan.service.TranslationService;
import net.bilgecan.service.UserService;
import net.bilgecan.support.NotificationSupport;
import net.bilgecan.view.components.PasswordResetDialog;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.Arrays;
import java.util.Locale;

@Route(value = "settings", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN"})
public class SettingsView extends VerticalLayout implements HasDynamicTitle {

    private final ComboBox<String> languagesComboBox;
    private final SecurityService securityService;
    private PasswordResetDialog passwordResetDialog;
    private TranslationService translations;
    private UserService userService;
    private final String AUTO = "AUTO";
    private final String EN = "EN";
    private final String TR = "TR";

    private Locale enLocale = Locale.ENGLISH;
    private Locale trLocale = new Locale("tr", "TR");

    public SettingsView(SecurityService securityService, UserService userService, TranslationService translations) {
        this.translations = translations;
        this.userService = userService;
        this.securityService = securityService;

        add(new H2(translations.t("settings.title")));

        languagesComboBox = new ComboBox<>(translations.t("settings.preferredLocale"));
        languagesComboBox.setItems(Arrays.asList(AUTO, TR, EN));
        languagesComboBox.setItemLabelGenerator((ItemLabelGenerator<String>) val -> translations.t("language." + val));

        Locale currentPreferredLanguage = securityService.getCurrentUser().getLocale();

        languagesComboBox.setValue(toLangValue(currentPreferredLanguage));

        Button saveButton = new Button(translations.t("button.save"));
        saveButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            String selectedLangValue = languagesComboBox.getValue();
            Locale selectedLanguage = toLocale(selectedLangValue);

            userService.updatePreferredLanguage(securityService.getCurrentUser(), selectedLanguage);
            UI.getCurrent().setLocale(selectedLanguage);
            UI.getCurrent().getPage().reload();
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Add empty space (spacer)
        Div spacer = new Div();
        spacer.setHeight("20px");

        Button passwordResetButton = new Button(translations.t("settings.resetPassword"));
        passwordResetButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        passwordResetButton.addClickListener(buttonClickEvent -> {
            User currentUser = securityService.getCurrentUser();
            passwordResetDialog = new PasswordResetDialog(translations, currentUser.getUsername(), true, (oldPassword, newPassword) -> {
                userService.resetCurrentUserPassword(oldPassword, newPassword);
                passwordResetDialog.close();
                NotificationSupport.showSuccess(translations.t("passwordReset.success"));
            });
            passwordResetDialog.open();
        });

        add(languagesComboBox);

        add(saveButton);
        add(spacer);
        add(passwordResetButton);

    }

    private Locale toLocale(String selectedLangValue) {
        if (AUTO.equals(selectedLangValue)) {
            return null;
        }
        if (TR.equals(selectedLangValue)) {
            return trLocale;
        }
        if (EN.equals(selectedLangValue)) {
            return enLocale;
        }
        return null;
    }

    private String toLangValue(Locale currentPreferedLanguage) {
        if (currentPreferedLanguage == null) {
            return AUTO;
        }
        if (currentPreferedLanguage.equals(trLocale)) {
            return TR;
        }
        if (currentPreferedLanguage.equals(enLocale)) {
            return EN;
        }
        return AUTO;
    }

    @Override
    public String getPageTitle() {
        return translations.t("menu.settings");
    }
}
