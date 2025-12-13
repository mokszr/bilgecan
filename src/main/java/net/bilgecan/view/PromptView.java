package net.bilgecan.view;

import net.bilgecan.dto.PromptDto;
import net.bilgecan.service.PromptService;
import net.bilgecan.service.TranslationService;
import net.bilgecan.support.NotificationSupport;
import net.bilgecan.view.components.PaginationView;
import net.bilgecan.view.components.SearchBarView;
import net.bilgecan.view.subviews.PromptViewDialog;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Route(value = "prompt", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN"})
public class PromptView extends VerticalLayout implements HasDynamicTitle {

    private PromptService promptService;
    private final TranslationService translations;
    private Binder<PromptDto> binder;
    private PaginationView paginationView;

    private PromptDto currentPrompt;
    private TextField name;
    private TextArea promptInputField;
    private Button saveButton;
    private Button cancelButton;
    private Grid<PromptDto> promptGrid;

    public PromptView(PromptService promptService, TranslationService translations) {
        this.promptService = promptService;
        this.translations = translations;
        setSizeFull();

        add(new H2(translations.t("prompt.yourPrompts")));

        HorizontalLayout toolbar = createToolbar();
        add(toolbar);

        createForm();

        SearchBarView searchBarView = new SearchBarView(translations) {

            @Override
            public void searchClicked(String searchValue) {
                paginationView.setSearchTerm(searchValue);
                long foundCount = paginationView.loadPage(0);
                NotificationSupport.showInfo(translations.t("general.search.foundNotification", foundCount));
            }
        };

        searchBarView.setJustifyContentMode(JustifyContentMode.END);

        add(searchBarView);

        hideForm();

        Grid<PromptDto> grid = createGrid();

        add(grid);

        paginationView = new PaginationView() {

            @Override
            protected Page loadAndGetPage(Pageable pageable, String searchTerm) {
                Page<PromptDto> termPage = promptService.findPaginated(pageable, searchTerm);
                // Update the grid with the current page data
                promptGrid.setItems(termPage.getContent());
                return termPage;
            }
        };
        paginationView.setCurrentPageKey("currentPageTermList");
        add(paginationView);
        paginationView.loadPage(paginationView.getCurrentPageInSession());
    }

    private void createForm() {

        name = new TextField(translations.t("prompt.name"));
        name.setWidthFull();

        binder = new Binder<>(PromptDto.class);
        binder.forField(name)
                // Finalize by doing the actual binding
                // to the Person class
                .withValidator(name -> StringUtils.isNotBlank(name), translations.t("validation.fieldCannotBeBlank", translations.t("prompt.name")))
                .bind(
                        // Callback that loads the title
                        // from a person instance
                        PromptDto::getName,
                        // Callback that saves the title
                        // in a person instance
                        PromptDto::setName);

        promptInputField = new TextArea(translations.t("prompt.input"));
        promptInputField.setPlaceholder(translations.t("prompt.inputPlaceholder"));
        promptInputField.setWidthFull();

        binder.forField(promptInputField).bind(
                PromptDto::getInput,
                PromptDto::setInput);
    }

    private HorizontalLayout createToolbar() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();

        Button addNewButton = new Button(translations.t("button.add"));

        addNewButton.addClickListener(ev -> openAddDialog(new PromptDto()));

        addNewButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        horizontalLayout.add(addNewButton);
        return horizontalLayout;
    }

    private void openAddDialog(PromptDto aPromptDto) {
        currentPrompt = aPromptDto;
        binder.readBean(currentPrompt);

        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setDraggable(true);
        dialog.setResizable(false);
        dialog.setWidth("90%");
        dialog.setMaxWidth("1000px");
        dialog.setMinWidth("300px");
        dialog.setHeaderTitle(translations.t("prompt.addPrompt"));


        saveButton = new Button(translations.t("button.save"));
        cancelButton = new Button(translations.t("button.cancel"));

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        saveButton.addClickShortcut(Key.ENTER);
        cancelButton.addClickShortcut(Key.ESCAPE);
        cancelButton.addClickListener(ev -> {
            hideForm();
            dialog.close();
        });

        saveButton.addClickListener(ev -> {
            boolean saved = savePrompt();
            if (saved) {
                dialog.close();
            }
        });

        HorizontalLayout buttons = new HorizontalLayout(cancelButton, saveButton);
        buttons.setJustifyContentMode(JustifyContentMode.END);
        buttons.setWidthFull();

        VerticalLayout dialogLayout = new VerticalLayout(name, promptInputField);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        dialog.add(dialogLayout, buttons);
        dialog.open();
    }

    private void hideForm() {
        // name.setValue("");
        //  promptInputField.setValue("");
    }

    private Grid<PromptDto> createGrid() {
        promptGrid = new Grid<>(PromptDto.class, false);
        promptGrid.setSizeFull();
        promptGrid.setEmptyStateText(translations.t("general.noItems"));

        promptGrid.addColumn(new ComponentRenderer<>(prompt -> {
            VerticalLayout hl = new VerticalLayout();
            hl.setSizeFull();
            H4 h4 = new H4(prompt.getName());
            h4.setSizeFull();
            h4.getStyle().set("text-overflow", "ellipsis");
            h4.getStyle().set("overflow-wrap", "break-word");
            h4.getStyle().set("white-space", "normal");

            hl.add(h4);

            Span countBadge = new Span(prompt.getInput());
            countBadge.getElement().getThemeList().add("badge pill small contrast");

            String counterLabel = translations.t("prompt.input", prompt.getInput());
            countBadge.getElement().setAttribute("aria-label", counterLabel);
            countBadge.getElement().setAttribute("title", counterLabel);

            hl.add(countBadge);

            return hl;
        })).setFlexGrow(1);

        promptGrid.addColumn(new ComponentRenderer<>(promptDto -> {
            HorizontalLayout actionsLayout = new HorizontalLayout();
            actionsLayout.setPadding(false);
            RouterLink routerLink = new RouterLink(PromptExecutionView.class, promptDto.getId());
            routerLink.add(VaadinIcon.ARROW_RIGHT.create());
            routerLink.getElement().setAttribute("title", translations.t("prompt.action.execute"));
            Div linkWrapper = new Div(routerLink);
            linkWrapper.addClickListener(event -> {
                // Additional actions before navigation
                paginationView.setCurrentPageInSession();
            });

            Icon viewIcon = VaadinIcon.OPEN_BOOK.create();
            viewIcon.setColor("grey");
            Button viewButton = new Button(viewIcon);


            viewButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
            viewButton.setTooltipText(translations.t("button.view"));
            viewButton.addClickListener(buttonClickEvent -> {
                PromptViewDialog viewDialog = new PromptViewDialog(promptDto, translations);
                viewDialog.open();
            });


            Icon deleteIcon = VaadinIcon.TRASH.create();
            deleteIcon.setColor("grey");

            Button deleteButton = new Button(deleteIcon);
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
            deleteButton.setTooltipText(translations.t("button.delete"));
            deleteButton.addClickListener(buttonClickEvent -> {
                deletePromptConfirmationDialog(promptDto);
            });

            Icon editIcon = VaadinIcon.EDIT.create();
            editIcon.setColor("grey");
            Button editButton = new Button(editIcon);
            editButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
            editButton.setTooltipText(translations.t("button.edit"));
            editButton.addClickListener(buttonClickEvent -> {
                openAddDialog(promptDto);
            });

            actionsLayout.add(deleteButton);
            actionsLayout.add(editButton);
            actionsLayout.add(viewButton);
            actionsLayout.add(linkWrapper);
            actionsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
            return actionsLayout;
        }
        ));

        return promptGrid;
    }


    private void deletePromptConfirmationDialog(PromptDto prompt) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(translations.t("prompt.deletePromptTitle"));
        dialog.setText(
                translations.t("prompt.deletePromptMessage", prompt.getName()));

        dialog.setCancelable(true);
        dialog.setCancelText(translations.t("button.cancel"));

        dialog.setConfirmText(translations.t("button.delete"));
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            promptService.delete(prompt);
            paginationView.loadCurrentPageAfterRemove();
            NotificationSupport.showSuccess(translations.t("notification.isDeleted", prompt.getName()));
        });

        dialog.open();
    }

    private boolean savePrompt() {
        try {
            binder.writeBean(currentPrompt);

            promptService.savePrompt(currentPrompt);
            paginationView.loadCurrentPageAfterAddition();
            hideForm();

            NotificationSupport.showSuccess(translations.t("notification.isSaved", translations.t("prompt.entityName")));
            return true;
        } catch (ValidationException ve) {
            ve.getValidationErrors().stream().forEach(v -> NotificationSupport.showError(v.getErrorMessage()));
            return false;
        }
    }

    @Override
    public String getPageTitle() {
        return translations.t("menu.prompts");
    }
}
