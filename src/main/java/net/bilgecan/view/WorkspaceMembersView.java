package net.bilgecan.view;

import net.bilgecan.dto.MembershipDto;
import net.bilgecan.dto.UserDto;
import net.bilgecan.dto.WorkspaceDto;
import net.bilgecan.entity.WorkspaceRole;
import net.bilgecan.exception.AppLevelValidationException;
import net.bilgecan.service.MembershipService;
import net.bilgecan.service.TranslationService;
import net.bilgecan.service.UserService;
import net.bilgecan.service.WorkspaceService;
import net.bilgecan.support.NotificationSupport;
import net.bilgecan.view.components.ComboBoxInputDialog;
import net.bilgecan.view.components.PaginationView;
import net.bilgecan.view.components.SearchBarView;
import net.bilgecan.view.subviews.SelectUserDialog;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;

@Route(value = "wsmembers", layout = MainLayout.class)
@RolesAllowed({"ADMIN"})
public class WorkspaceMembersView extends VerticalLayout implements HasDynamicTitle, HasUrlParameter<Long> {

    private final Span workspaceNameSpan;
    private final SearchBarView searchBarView;
    private final PaginationView paginationView;
    private final Grid<MembershipDto> wsMembersGrid;
    private final WorkspaceService workspaceService;
    private MembershipService membershipService;
    private UserService userService;
    private TranslationService translations;
    private Long workspaceId;
    private VerticalLayout formLayout;
    private MembershipDto currentMembership;
    private UserDto selectedUserDto;
    private H4 selectedUserH4;
    private ComboBox<WorkspaceRole> workspaceRoleComboBox;

    public WorkspaceMembersView(MembershipService membershipService, WorkspaceService workspaceService, UserService userService, TranslationService translations) {
        this.membershipService = membershipService;
        this.translations = translations;
        this.workspaceService = workspaceService;
        this.userService = userService;
        setSizeFull();
        add(new H2(translations.t("wsMember.title")));

        workspaceNameSpan = new Span(translations.t("wsMember.wsName"));
        add(workspaceNameSpan);

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

        wsMembersGrid = createGrid();
        add(wsMembersGrid);

        paginationView = new PaginationView() {

            @Override
            protected Page loadAndGetPage(Pageable pageable, String searchTerm) {
                Page<MembershipDto> termPage = membershipService.findPaginated(pageable, searchTerm, workspaceId);
                // Update the grid with the current page data
                wsMembersGrid.setItems(termPage.getContent());
                return termPage;
            }
        };
        paginationView.setCurrentPageKey("currentPageWSMembers");
        add(paginationView);

    }

    private void createForm() {
        formLayout = new VerticalLayout();
        formLayout.setSizeFull();
        formLayout.setVisible(false);

        HorizontalLayout userSelectLayout = new HorizontalLayout();
        userSelectLayout.setPadding(false);
        userSelectLayout.setMargin(false);
        userSelectLayout.setAlignItems(Alignment.END);

        selectedUserH4 = new H4(translations.t("wsMember.selectedUser"));
        Button selectUserButton = new Button(translations.t("wsMember.selectUser"));
        selectUserButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        selectUserButton.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                openUserSelectDialog();
            }
        });

        userSelectLayout.add(selectedUserH4);
        userSelectLayout.add(selectUserButton);

        workspaceRoleComboBox = new ComboBox<>(translations.t("wsMember.workspaceRole"));
        workspaceRoleComboBox.setItems(WorkspaceRole.values());
        workspaceRoleComboBox.setValue(WorkspaceRole.EDITOR);

        Button saveButton = new Button(translations.t("button.save"));
        Button cancelButton = new Button(translations.t("button.cancel"));

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        cancelButton.addClickShortcut(Key.ESCAPE);
        cancelButton.addClickListener(ev -> {
            hideForm();
        });

        saveButton.addClickListener(ev -> {
            boolean saved = saveMembership();
            if (saved) {
                hideForm();
            }
        });

        HorizontalLayout buttons = new HorizontalLayout(cancelButton, saveButton);
        buttons.setJustifyContentMode(JustifyContentMode.END);
        buttons.setWidthFull();

        formLayout.add(userSelectLayout);
        formLayout.add(workspaceRoleComboBox);
        formLayout.add(buttons);

        add(formLayout);
    }

    private boolean saveMembership() {
        if (selectedUserDto == null) {
            NotificationSupport.showError(translations.t("wsMember.pleaseSelectAUser"));
            return false;
        }
        if (workspaceRoleComboBox.getValue() == null) {
            NotificationSupport.showError(translations.t("wsMember.pleaseSelectWorkspaceRole"));
            return false;
        }

        membershipService.saveMembership(workspaceId, selectedUserDto, workspaceRoleComboBox.getValue());
        paginationView.loadCurrentPageAfterAddition();
        hideForm();

        NotificationSupport.showSuccess(translations.t("notification.isSaved", translations.t("wsMember.entityName")));
        return true;
    }

    private void openUserSelectDialog() {
        SelectUserDialog selectUserDialog = new SelectUserDialog(userService, selectedUser -> {
            selectedUserDto = selectedUser;
            selectedUserH4.setText(translations.t("wsMember.selectedUser") + " " + selectedUserDto.getUsername());
        }, translations);
        selectUserDialog.open();
    }

    private void hideForm() {
        formLayout.setVisible(false);
        searchBarView.setVisible(true);
    }

    private Grid<MembershipDto> createGrid() {
        Grid<MembershipDto> grid = new Grid<>(MembershipDto.class, false);
        grid.setSizeFull();
        grid.setEmptyStateText(translations.t("general.noItems"));

        grid.addColumn(MembershipDto::getUsername)
                .setHeader(translations.t("wsMember.columns.username"))
                .setTooltipGenerator(MembershipDto::getUsername)
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(MembershipDto::getRole)
                .setHeader(translations.t("wsMember.columns.role"))
                .setTooltipGenerator(membershipDto -> membershipDto.getRole().name())
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(membershipDto -> {
                    HorizontalLayout actionsLayout = new HorizontalLayout();

                    Icon deleteIcon = VaadinIcon.TRASH.create();
                    deleteIcon.setColor("gray");
                    Button deleteButton = new Button(deleteIcon);
                    deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
                    deleteButton.setTooltipText(translations.t("button.delete"));

                    deleteButton.addClickListener(e -> {
                        deleteMembershipConfirmationDialog(membershipDto);
                    });

                    Icon editIcon = VaadinIcon.EDIT.create();
                    editIcon.setColor("gray");
                    Button editButton = new Button(editIcon);
                    editButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
                    editButton.setTooltipText(translations.t("button.edit"));
                    editButton.addClickListener(e -> {
                        openEditDialog(membershipDto);
                    });

                    actionsLayout.add(deleteButton);
                    actionsLayout.add(editButton);
                    actionsLayout.setJustifyContentMode(JustifyContentMode.END);
                    return actionsLayout;
                }
                )).setHeader(translations.t("general.columns.actions"))
                .setAutoWidth(true)
                .setFlexGrow(0);

        return grid;
    }

    private void openEditDialog(MembershipDto membershipDto) {
        ComboBoxInputDialog<WorkspaceRole> dialog =
                new ComboBoxInputDialog<>(
                        translations.t("wsMember.selectWorkspaceRole"),
                        translations.t("wsMember.workspaceRole"),
                        Arrays.asList(WorkspaceRole.values()),
                        membershipDto.getRole(),
                        selected -> {
                            membershipService.updateMembership(membershipDto, selected);
                            NotificationSupport.showSuccess(translations.t("wsMember.roleUpdated"));
                            paginationView.loadCurrentPageAfterAddition();
                        },
                        translations
                );

        dialog.open();
    }

    private HorizontalLayout createToolbar() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();

        Button addNewButton = new Button(translations.t("button.add"));

        addNewButton.addClickListener(ev -> openFormView(new MembershipDto()));

        addNewButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        horizontalLayout.add(addNewButton);
        return horizontalLayout;
    }

    private void openFormView(MembershipDto membershipDto) {
        currentMembership = membershipDto;
        readBean(currentMembership);

        formLayout.setVisible(true);
        searchBarView.setVisible(false);
    }

    private void readBean(MembershipDto currentMembership) {
        if (currentMembership.getUsername() == null) {
            selectedUserH4.setText(translations.t("wsMember.selectedUser"));
        } else {
            selectedUserH4.setText(translations.t("wsMember.selectedUser") + " " + currentMembership.getUsername());
        }

        workspaceRoleComboBox.setValue(currentMembership.getRole());

    }

    private void deleteMembershipConfirmationDialog(MembershipDto membershipDto) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(translations.t("wsMember.deleteMembershipTitle"));
        dialog.setText(
                translations.t("wsMember.deleteMembershipMessage", membershipDto.getUsername()));

        dialog.setCancelable(true);
        dialog.setCancelText(translations.t("button.cancel"));

        dialog.setConfirmText(translations.t("button.delete"));
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            membershipService.delete(membershipDto);
            paginationView.loadCurrentPageAfterRemove();
            NotificationSupport.showSuccess(translations.t("notification.isDeleted", membershipDto.getUsername()));
        });

        dialog.open();
    }


    @Override
    public String getPageTitle() {
        return translations.t("menu.workspacemembers");
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long workspaceId) {
        if (workspaceId == null) {
            NotificationSupport.showError(translations.t("workspace.noWorkspaceFound"));
            return;
        }
        this.workspaceId = workspaceId;
        try {
            WorkspaceDto workspace = workspaceService.getWorkspace(workspaceId);

            workspaceNameSpan.setText(translations.t("wsMember.wsName") + " " + workspace.getName());
            paginationView.loadPage(paginationView.getCurrentPageInSession());
        } catch (AppLevelValidationException alve) {
            workspaceNameSpan.setText(alve.getMessage());
        }

    }
}
