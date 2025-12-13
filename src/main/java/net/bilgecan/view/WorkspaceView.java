package net.bilgecan.view;

import net.bilgecan.dto.WorkspaceDto;
import net.bilgecan.service.TranslationService;
import net.bilgecan.service.WorkspaceService;
import net.bilgecan.support.NotificationSupport;
import net.bilgecan.view.components.PaginationView;
import net.bilgecan.view.components.SearchBarView;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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

@Route(value = "workspaces", layout = MainLayout.class)
@RolesAllowed({"ADMIN"})
public class WorkspaceView extends VerticalLayout implements HasDynamicTitle {

    private final SearchBarView searchBarView;
    private final PaginationView paginationView;
    private WorkspaceService workspaceService;
    private TranslationService translations;
    private WorkspaceDto currentWorkspace;
    private Binder<WorkspaceDto> binder;
    private VerticalLayout formLayout;
    private TextField nameField;
    private Grid<WorkspaceDto> workspaceGrid;
    private TextField slugField;

    public WorkspaceView(WorkspaceService workspaceService, TranslationService translations) {
        this.translations = translations;
        this.workspaceService = workspaceService;
        setSizeFull();
        add(new H2(translations.t("workspace.title")));

        HorizontalLayout toolbar = createToolbar();
        add(toolbar);

        createForm();

        searchBarView = new SearchBarView(translations) {

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

        workspaceGrid = createGrid();

        add(workspaceGrid);

        paginationView = new PaginationView() {

            @Override
            protected Page loadAndGetPage(Pageable pageable, String searchTerm) {
                Page<WorkspaceDto> termPage = workspaceService.findPaginated(pageable, searchTerm);
                // Update the grid with the current page data
                workspaceGrid.setItems(termPage.getContent());
                return termPage;
            }
        };
        paginationView.setCurrentPageKey("currentPageWorkspace");
        add(paginationView);
        paginationView.loadPage(paginationView.getCurrentPageInSession());
    }

    private void createForm() {
        formLayout = new VerticalLayout();
        formLayout.setSizeFull();
        formLayout.setVisible(false);

        nameField = new TextField(translations.t("workspace.name"));
        nameField.setWidthFull();

        binder = new Binder<>(WorkspaceDto.class);
        binder.forField(nameField)
                .withValidator(StringUtils::isNotBlank, translations.t("validation.fieldCannotBeBlank", translations.t("workspace.name")))
                .bind(
                        WorkspaceDto::getName,
                        WorkspaceDto::setName);

        slugField = new TextField(translations.t("workspace.slug"));
        slugField.setWidthFull();

        binder.forField(slugField)
                .withValidator(StringUtils::isNotBlank, translations.t("validation.fieldCannotBeBlank", translations.t("workspace.slug")))
                .bind(
                        WorkspaceDto::getSlug,
                        WorkspaceDto::setSlug);

        Button saveButton = new Button(translations.t("button.save"));
        Button cancelButton = new Button(translations.t("button.cancel"));

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        cancelButton.addClickShortcut(Key.ESCAPE);
        cancelButton.addClickListener(ev -> {
            hideForm();
        });

        saveButton.addClickListener(ev -> {
            boolean saved = saveWorkspace();
            if (saved) {
                hideForm();
            }
        });

        HorizontalLayout buttons = new HorizontalLayout(cancelButton, saveButton);
        buttons.setJustifyContentMode(JustifyContentMode.END);
        buttons.setWidthFull();

        formLayout.add(nameField);
        formLayout.add(slugField);
        formLayout.add(buttons);

        add(formLayout);
    }

    private Grid<WorkspaceDto> createGrid() {
        Grid<WorkspaceDto> workspaceGrid = new Grid<>(WorkspaceDto.class, false);
        workspaceGrid.setSizeFull();
        workspaceGrid.setEmptyStateText(translations.t("general.noItems"));

        workspaceGrid.addColumn(WorkspaceDto::getName)
                .setHeader(translations.t("workspace.columns.name"))
                .setTooltipGenerator(WorkspaceDto::getName)
                .setAutoWidth(true)
                .setFlexGrow(1);

        workspaceGrid.addColumn(WorkspaceDto::getSlug)
                .setHeader(translations.t("workspace.columns.slug"))
                .setTooltipGenerator(WorkspaceDto::getName)
                .setAutoWidth(true);

        workspaceGrid.addColumn(new ComponentRenderer<>(workspaceDto -> {
                    HorizontalLayout actionsLayout = new HorizontalLayout();

                    Icon deleteIcon = VaadinIcon.TRASH.create();
                    deleteIcon.setColor("gray");
                    Button deleteButton = new Button(deleteIcon);
                    deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
                    deleteButton.setTooltipText(translations.t("button.delete"));

                    deleteButton.addClickListener(e -> {
                        deleteWorkspaceConfirmationDialog(workspaceDto);
                    });

                    Icon editIcon = VaadinIcon.EDIT.create();
                    editIcon.setColor("gray");
                    Button editButton = new Button(editIcon);
                    editButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
                    editButton.setTooltipText(translations.t("button.edit"));
                    editButton.addClickListener(e -> {
                        openFormView(workspaceDto);
                    });

                    RouterLink routerLink = new RouterLink(WorkspaceMembersView.class, workspaceDto.getId());
                    Icon usersIcon = VaadinIcon.USERS.create();
                    usersIcon.setColor("gray");
                    routerLink.add(usersIcon);
                    routerLink.getElement().setAttribute("title", translations.t("workspace.action.members"));
                    Div linkWrapper = new Div(routerLink);
                    linkWrapper.addClickListener(event -> {
                        // Additional actions before navigation
                        paginationView.setCurrentPageInSession();
                    });


                    actionsLayout.add(deleteButton);
                    actionsLayout.add(editButton);
                    actionsLayout.add(linkWrapper);
                    actionsLayout.setJustifyContentMode(JustifyContentMode.END);
                    return actionsLayout;
                }
                )).setHeader(translations.t("workspace.columns.actions"))
                .setAutoWidth(true)
                .setFlexGrow(0);

        return workspaceGrid;
    }

    private void deleteWorkspaceConfirmationDialog(WorkspaceDto workspaceDto) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(translations.t("workspace.deleteWorkspaceTitle"));
        dialog.setText(
                translations.t("workspace.deleteWorkspaceMessage", workspaceDto.getName()));

        dialog.setCancelable(true);
        dialog.setCancelText(translations.t("button.cancel"));

        dialog.setConfirmText(translations.t("button.delete"));
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            workspaceService.delete(workspaceDto);
            paginationView.loadCurrentPageAfterRemove();
            NotificationSupport.showSuccess(translations.t("notification.isDeleted", workspaceDto.getName()));
        });

        dialog.open();
    }

    private HorizontalLayout createToolbar() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();

        Button addNewButton = new Button(translations.t("button.add"));

        addNewButton.addClickListener(ev -> openFormView(new WorkspaceDto()));

        addNewButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        horizontalLayout.add(addNewButton);
        return horizontalLayout;
    }

    private void openFormView(WorkspaceDto workspaceDto) {
        currentWorkspace = workspaceDto;
        binder.readBean(currentWorkspace);

        formLayout.setVisible(true);
        searchBarView.setVisible(false);
    }


    private void hideForm() {
        formLayout.setVisible(false);
        searchBarView.setVisible(true);
    }

    private boolean saveWorkspace() {
        try {
            binder.writeBean(currentWorkspace);

            workspaceService.saveWorkspace(currentWorkspace);
            paginationView.loadCurrentPageAfterAddition();
            hideForm();

            NotificationSupport.showSuccess(translations.t("notification.isSaved", translations.t("workspace.entityName")));
            return true;
        } catch (ValidationException ve) {
            ve.getValidationErrors().stream().forEach(v -> NotificationSupport.showError(v.getErrorMessage()));
            return false;
        }
    }

    @Override
    public String getPageTitle() {
        return translations.t("menu.workspaces");
    }
}
