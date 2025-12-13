package net.bilgecan.view.subviews;

import net.bilgecan.service.TranslationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.Anchor;

public class ToBeImplementedView extends VerticalLayout {

    public ToBeImplementedView(TranslationService translations) {

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");

        // Icon
        Icon wrenchIcon = VaadinIcon.TOOLS.create();
        wrenchIcon.setSize("44px");

        // Title & subtitle
        H2 title = new H2(translations.t("toBeImplemented.title"));
        Paragraph subtitle = new Paragraph(
                translations.t("toBeImplemented.subtitle")
        );

        // Extra explanation text
        Paragraph helper = new Paragraph(
                translations.t("toBeImplemented.helper"));
        helper.setMaxWidth("600px");

        // Actions
        Button feedbackButton = new Button(translations.t("toBeImplemented.feedbackButton"), VaadinIcon.LIGHTBULB.create());
        feedbackButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        feedbackButton.addClickListener(e ->
                getUI().ifPresent(ui ->
                        ui.getPage().open("mailto:muratoksuzer01@gmail.com?subject=Feature%20request", "_blank")
                )
        );

        // Replace URLs with your own GitHub / BuyMeACoffee links
        Anchor githubAnchor = new Anchor("https://github.com/mokszr/bilgecan", "");
        Button githubButton = new Button(translations.t("toBeImplemented.githubButton"));
        githubButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        githubAnchor.add(githubButton);
        githubAnchor.setTarget("_blank");

        Anchor supportAnchor = new Anchor("https://buymeacoffee.com/muratoksuzer", "");
        Button supportButton = new Button(translations.t("toBeImplemented.supportButton"));
        supportButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY);
        supportAnchor.add(supportButton);
        supportAnchor.setTarget("_blank");

        HorizontalLayout actions = new HorizontalLayout(feedbackButton, githubAnchor, supportAnchor);
        actions.setJustifyContentMode(JustifyContentMode.CENTER);
        actions.setAlignItems(Alignment.CENTER);
        actions.setSpacing(true);
        actions.getStyle().set("flex-wrap", "wrap");

        // Center everything vertically as well
        FlexLayout centerWrapper = new FlexLayout();
        centerWrapper.setSizeFull();
        centerWrapper.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        centerWrapper.setJustifyContentMode(JustifyContentMode.CENTER);
        centerWrapper.setAlignItems(Alignment.CENTER);

        centerWrapper.add(wrenchIcon, title, subtitle, helper, actions);

        add(centerWrapper);
    }
}
