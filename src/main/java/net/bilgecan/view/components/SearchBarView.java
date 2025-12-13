package net.bilgecan.view.components;

import net.bilgecan.service.TranslationService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

public abstract class SearchBarView extends HorizontalLayout {

    private final TranslationService translations;

    public SearchBarView (TranslationService translations) {
        this.translations = translations;
        TextField textField = new TextField();
        textField.setPlaceholder(translations.t("general.search.placeholder"));
        textField.setClearButtonVisible(true);
        textField.setPrefixComponent(VaadinIcon.SEARCH.create());

        Button searchButton = new Button(translations.t("general.search.button"),
                new Icon(VaadinIcon.SEARCH));
        searchButton.setIconAfterText(true);

        searchButton.addClickListener(e -> searchClicked(textField.getValue().trim()));
        searchButton.addClickShortcut(Key.ENTER).listenOn(textField);
        add(textField);
        add(searchButton);

        setFlexGrow(1, textField);

        setWidthFull();
    }

    public abstract void searchClicked(String searchValue);
}
