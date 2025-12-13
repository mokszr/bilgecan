package net.bilgecan.view.subviews;

import net.bilgecan.service.TranslationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class CallToActionView extends HorizontalLayout {

    public CallToActionView(TranslationService translations) {
        setAlignItems(FlexComponent.Alignment.CENTER);
        setSpacing(true);
        getStyle().set("gap", "1rem");

        // Email button
        Button mailBtn = new Button(translations.t("callToAction.email"), VaadinIcon.ENVELOPE.create(), e ->
                getUI().ifPresent(ui -> ui.getPage().open("mailto:muratoksuzer01@gmail.com?subject=About%20Bilgecan", "_blank"))
        );
        mailBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);

        // GitHub star button
        Button githubBtn = new Button(translations.t("callToAction.github"), VaadinIcon.STAR.create(), e ->
                getUI().ifPresent(ui -> ui.getPage().open("https://github.com/mokszr/bilgecan", "_blank"))
        );
        githubBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);

        // Buy Me a Coffee button
        Button coffeeBtn = new Button(translations.t("callToAction.support"), VaadinIcon.COFFEE.create(), e ->
                getUI().ifPresent(ui ->ui.getPage().open("https://buymeacoffee.com/muratoksuzer", "_blank"))
        );
        coffeeBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);

        add(mailBtn, githubBtn, coffeeBtn);
    }
}
