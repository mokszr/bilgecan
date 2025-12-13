package net.bilgecan.view.subviews;

import net.bilgecan.dto.UserDto;
import net.bilgecan.service.TranslationService;
import net.bilgecan.service.UserService;
import net.bilgecan.support.NotificationSupport;
import net.bilgecan.view.components.PaginationView;
import net.bilgecan.view.components.SearchBarView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.function.Consumer;

public class SelectUserDialog extends Dialog {

    private Grid<UserDto> userGrid;
    private final SearchBarView searchBarView;
    private final PaginationView paginationView;
    private TranslationService translations;
    private UserService userService;
    private Consumer<UserDto> selectionHandler;

    public SelectUserDialog(UserService userService, Consumer<UserDto> selectionHandler, TranslationService translations) {
        this.translations = translations;
        this.userService = userService;
        this.selectionHandler = selectionHandler;
        setModal(true);
        setDraggable(true);
        setResizable(false);
        setWidth("90%");
        setMaxWidth("1000px");
        setMinWidth("300px");
        setMinHeight("600px");
        setHeaderTitle(translations.t("selectUserDialog.title"));

        searchBarView = new SearchBarView(translations) {

            @Override
            public void searchClicked(String searchValue) {
                paginationView.setSearchTerm(searchValue);
                long foundCount = paginationView.loadPage(0);
                NotificationSupport.showInfo(translations.t("general.search.foundNotification", foundCount));
            }
        };

        searchBarView.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        add(searchBarView);

        userGrid = createGrid();
        add(userGrid);

        paginationView = new PaginationView() {

            @Override
            protected Page loadAndGetPage(Pageable pageable, String searchTerm) {
                Page<UserDto> termPage = userService.findPaginated(pageable, searchTerm);
                // Update the grid with the current page data
                userGrid.setItems(termPage.getContent());
                return termPage;
            }
        };

        paginationView.setCurrentPageKey("currentPageSelectUser");
        paginationView.setPageSize(4);
        paginationView.loadPage(paginationView.getCurrentPageInSession());

        Button cancelButton = new Button(translations.t("button.cancel"));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(ev -> {
            close();
        });

        HorizontalLayout buttons = new HorizontalLayout(cancelButton);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.setWidthFull();

        VerticalLayout dialogLayout = new VerticalLayout(searchBarView, userGrid, paginationView);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);
        dialogLayout.setWidthFull();
        dialogLayout.setMaxHeight("500px");

        add(dialogLayout, buttons);
    }

    private Grid<UserDto> createGrid() {
        Grid<UserDto> userGrid = new Grid<>(UserDto.class, false);
        userGrid.setWidthFull();
        userGrid.setMaxHeight("500px");
        userGrid.setEmptyStateText(translations.t("general.noItems"));

        userGrid.addColumn(UserDto::getUsername)
                .setHeader(translations.t("users.columns.username"))
                .setTooltipGenerator(UserDto::getUsername)
                .setAutoWidth(true)
                .setFlexGrow(1);

        userGrid.addColumn(UserDto::getEmail)
                .setHeader(translations.t("users.columns.email"))
                .setTooltipGenerator(UserDto::getEmail)
                .setAutoWidth(true);

        userGrid.addColumn(userDto -> translations.t("users.enabled." + userDto.isEnabled()))
                .setHeader(translations.t("users.columns.enabled"))
                .setTooltipGenerator(userDto -> translations.t("users.enabled." + userDto.isEnabled()))
                .setAutoWidth(true)
                .setFlexGrow(1);


        userGrid.addColumn(new ComponentRenderer<>(userDto -> {
                    Icon arrowRightIcon = VaadinIcon.ARROW_RIGHT.create();
                    arrowRightIcon.setColor("grey");

                    Button chooseButton = new Button(translations.t("button.select"), arrowRightIcon);
                    chooseButton.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
                        @Override
                        public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                            userSelected(userDto);
                        }
                    });

                    return chooseButton;
                }
                )).setHeader(translations.t("users.columns.actions"))
                .setAutoWidth(true)
                .setFlexGrow(0);

        return userGrid;
    }

    private void userSelected(UserDto userDto) {
        selectionHandler.accept(userDto);
        this.close();
    }
}
