package net.bilgecan.view.components;

import net.bilgecan.service.TranslationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.function.Consumer;

public class ComboBoxInputDialog<T> extends Dialog {

    private final ComboBox<T> comboBox = new ComboBox<>();
    private final Button saveButton;
    private final Button cancelButton;

    public ComboBoxInputDialog(
            String title,
            String label,
            List<T> items,
            T selectedValue,
            Consumer<T> onSubmit,
            TranslationService translations
    ) {
        setHeaderTitle(title);
        setModal(true);
        setWidth("400px");
        saveButton = new Button(translations.t("button.save"));
        cancelButton = new Button(translations.t("button.cancel"));

        comboBox.setLabel(label);
        comboBox.setItems(items);
        if(selectedValue != null) {
            comboBox.setValue(selectedValue);
        }
        comboBox.setWidthFull();

        saveButton.addClickListener(e -> {
            T value = comboBox.getValue();
            if (value == null) {
                comboBox.setInvalid(true);
                comboBox.setErrorMessage(translations.t("general.pleaseChooseAValue"));
                return;
            }
            onSubmit.accept(value);
            close();
        });

        cancelButton.addClickListener(e -> close());

        HorizontalLayout footer = new HorizontalLayout(cancelButton, saveButton);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footer.setWidthFull();

        VerticalLayout layout = new VerticalLayout(comboBox);
        layout.setPadding(false);
        layout.setSpacing(true);

        add(layout);
        getFooter().add(footer);
    }
}
