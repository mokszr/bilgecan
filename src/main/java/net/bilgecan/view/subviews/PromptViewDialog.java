package net.bilgecan.view.subviews;

import com.vaadin.flow.component.markdown.Markdown;
import net.bilgecan.dto.PromptDto;
import net.bilgecan.service.TranslationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class PromptViewDialog extends Dialog {

    public PromptViewDialog(PromptDto dto, TranslationService translations) {
        setModal(true);
        setDraggable(true);
        setResizable(false);

        setWidth("90%");
        setMaxWidth("1000px");
        setMinWidth("300px");
        setHeight("600px");
        setHeaderTitle(translations.t("prompt.promptViewDialog"));

        Button cancelButton = new Button(translations.t("button.cancel"));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(ev -> {
            close();
        });

        HorizontalLayout buttons = new HorizontalLayout(cancelButton);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.setWidthFull();

        Card card = new Card();
        card.setWidthFull();
        card.setMaxHeight("500px");
        card.setTitle(dto.getName());
        Markdown p1 = new Markdown(dto.getInput());
        p1.setMaxHeight("400px");
        p1.setWidthFull();
        p1.getStyle().set("text-overflow", "ellipsis");
        p1.getStyle().set("overflow-wrap", "break-word");
        p1.getStyle().set("white-space", "normal");
        p1.getStyle().set("overflow", "auto");
        card.add(p1);

        VerticalLayout dialogLayout = new VerticalLayout(card);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);
        dialogLayout.setWidthFull();
        dialogLayout.setMaxHeight("500px");

        add(dialogLayout, buttons);
    }
}
