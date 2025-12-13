package net.bilgecan.view;

import net.bilgecan.config.ConstantConfigs;
import net.bilgecan.config.RoleConstant;
import net.bilgecan.dto.UserDto;
import net.bilgecan.service.TranslationService;
import net.bilgecan.service.UserService;
import net.bilgecan.support.NotificationSupport;
import net.bilgecan.view.components.PaginationView;
import net.bilgecan.view.components.PasswordResetDialog;
import net.bilgecan.view.components.SearchBarView;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Route(value = "users", layout = MainLayout.class)
@RolesAllowed({"ADMIN"})
public class UserManagementView extends VerticalLayout implements HasDynamicTitle {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final SearchBarView searchBarView;
    private final UserService userService;
    private final TranslationService translations;
    private PaginationView paginationView;

    private UserDto currentUser;
    private Button saveButton;
    private Button cancelButton;
    private Grid<UserDto> userGrid;
    private FormLayout formLayout;
    private TextField usernameField;
    private EmailField emailField;
    private PasswordField passwordField;
    private CheckboxGroup<String> rolesCheckBoxGroup;
    private PasswordResetDialog passwordResetDialog;


    public UserManagementView(UserService userService,
                              TranslationService translations) {
        this.userService = userService;
        this.translations = translations;
        setSizeFull();

        add(new H2(translations.t("users.userManagementTitle")));

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

        Grid<UserDto> grid = createGrid();

        add(grid);

        paginationView = new PaginationView() {

            @Override
            protected Page loadAndGetPage(Pageable pageable, String searchTerm) {
                Page<UserDto> termPage = userService.findPaginated(pageable, searchTerm);
                // Update the grid with the current page data
                userGrid.setItems(termPage.getContent());
                return termPage;
            }
        };
        paginationView.setCurrentPageKey("currentPageAITaskTemplate");
        add(paginationView);
        paginationView.loadPage(paginationView.getCurrentPageInSession());
    }

    private void createForm() {

        usernameField = new TextField(translations.t("general.username"));
        usernameField.setSizeFull();
        usernameField.setPlaceholder(translations.t("signup.username.placeholder"));
        usernameField.setRequired(true);
        usernameField.getElement().setAttribute("autocomplete", "off");
        usernameField.setMaxLength(ConstantConfigs.MAX_USERNAME_LENGTH);
        usernameField.setMinLength(ConstantConfigs.MIN_USERNAME_LENGTH);
        usernameField.setPattern(ConstantConfigs.USERNAME_REGEX);

        usernameField.setI18n(new TextField.TextFieldI18n()
                .setRequiredErrorMessage(translations.t("general.required"))
                .setMinLengthErrorMessage(translations.t("signup.username.minimumLength"))
                .setMaxLengthErrorMessage(translations.t("signup.username.maximumLength"))
                .setPatternErrorMessage(translations.t("signup.username.patternErrorMessage")));

        emailField = new EmailField(translations.t("general.email"));
        emailField.setSizeFull();
        emailField.setPlaceholder(translations.t("signup.email.placeholder"));
        emailField.setRequired(true);
        emailField.setErrorMessage(translations.t("signup.email.errorMessage"));
        emailField.getElement().setAttribute("autocomplete", "email");


        passwordField = new PasswordField(translations.t("general.password"));
        passwordField.setPlaceholder(translations.t("signup.password.placeholder"));
        passwordField.setRequired(true);
        passwordField.getElement().setAttribute("autocomplete", "new-password");
        passwordField.setPattern(ConstantConfigs.PASSWORD_REGEX);
        passwordField.setMinLength(ConstantConfigs.MIN_PASSWORD_LENGTH);
        passwordField.setMaxLength(ConstantConfigs.MAX_PASSWORD_LENGTH);

        passwordField.setI18n(new PasswordField.PasswordFieldI18n()
                .setRequiredErrorMessage(translations.t("general.required"))
                .setMinLengthErrorMessage(translations.t("signup.password.minimumLength"))
                .setMaxLengthErrorMessage(translations.t("signup.password.maximumLength"))
                .setPatternErrorMessage(translations.t("signup.password.patternErrorMessage")));
        passwordField.setHelperText(translations.t("signup.password.helperText"));

        rolesCheckBoxGroup = new CheckboxGroup<>();
        rolesCheckBoxGroup.setLabel(translations.t("users.roles"));
        rolesCheckBoxGroup.setItems(RoleConstant.ROLE_USER, RoleConstant.ROLE_ADMIN);
        rolesCheckBoxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        rolesCheckBoxGroup.select(RoleConstant.ROLE_USER);
        rolesCheckBoxGroup.setRequired(true);
        rolesCheckBoxGroup.setErrorMessage(translations.t("general.required"));

        saveButton = new Button(translations.t("button.save"));
        cancelButton = new Button(translations.t("button.cancel"));

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        cancelButton.addClickShortcut(Key.ESCAPE);
        cancelButton.addClickListener(ev -> {
            hideForm();
        });

        saveButton.addClickListener(ev -> {

            boolean isValid = true;

            // Validate email
            if (emailField.isEmpty() || emailField.isInvalid()) {
                emailField.setInvalid(true);
                isValid = false;
            } else {
                emailField.setInvalid(false);
            }

            // Validate username
            if (usernameField.isEmpty() || usernameField.getValue().length() < ConstantConfigs.MIN_USERNAME_LENGTH
                    || usernameField.getValue().length() > ConstantConfigs.MAX_USERNAME_LENGTH
                    || !ConstantConfigs.USERNAME_POLICY.matcher(usernameField.getValue()).matches()) {
                usernameField.setInvalid(true);
                isValid = false;
            } else {
                usernameField.setInvalid(false);
            }

            //validate password when saving new user
            if (currentUser.getId() == null) {
                // Validate password
                if (!ConstantConfigs.PASSWORD_POLICY.matcher(passwordField.getValue()).matches()) {
                    passwordField.setInvalid(true);
                    isValid = false;
                } else {
                    passwordField.setInvalid(false);
                }
            }

            if (rolesCheckBoxGroup.getValue().isEmpty()) {
                isValid = false;
                rolesCheckBoxGroup.setInvalid(true);
            }

            if (isValid) {
                boolean saved = saveUser();
                if (saved) {
                    hideForm();
                }
            }
        });

        HorizontalLayout buttons = new HorizontalLayout(cancelButton, saveButton);
        buttons.setJustifyContentMode(JustifyContentMode.END);
        buttons.setWidthFull();

        formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1)     // Mobile → single column
        );
        formLayout.add(emailField, usernameField, passwordField, rolesCheckBoxGroup, buttons);

        add(formLayout);
    }

    private HorizontalLayout createToolbar() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();

        Button addNewButton = new Button(translations.t("button.add"));
        addNewButton.addClickListener(ev -> openFormView(new UserDto()));

        addNewButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        horizontalLayout.add(addNewButton);
        return horizontalLayout;
    }

    private void openFormView(UserDto aUserDto) {
        currentUser = aUserDto;
        readBean(currentUser);

        //for fresh dto, make fields valid
        if (aUserDto.getId() == null) {
            emailField.setInvalid(false);
            usernameField.setInvalid(false);
            passwordField.setInvalid(false);
            rolesCheckBoxGroup.setInvalid(false);
        }

        formLayout.setVisible(true);
        searchBarView.setVisible(false);
    }


    private void hideForm() {
        formLayout.setVisible(false);
        searchBarView.setVisible(true);
    }

    private Grid<UserDto> createGrid() {
        userGrid = new Grid<>(UserDto.class, false);
        userGrid.setSizeFull();
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

        userGrid.addColumn(userDto -> String.join(",", userDto.getRoles()))
                .setHeader(translations.t("users.columns.roles"))
                .setTooltipGenerator(userDto -> String.join(",", userDto.getRoles()))
                .setAutoWidth(true);

        userGrid.addColumn(userDto -> translations.t("users.enabled." + userDto.isEnabled()))
                .setHeader(translations.t("users.columns.enabled"))
                .setTooltipGenerator(userDto -> translations.t("users.enabled." + userDto.isEnabled()))
                .setAutoWidth(true);

        userGrid.addColumn(dto -> formatter.withZone(ZoneId.systemDefault()).format(dto.getDateCreated()))
                .setHeader(translations.t("users.columns.createdDate"))
                .setAutoWidth(true);

        userGrid.addColumn(new ComponentRenderer<>(userDto -> {
                    HorizontalLayout actionsLayout = new HorizontalLayout();
                    actionsLayout.setSpacing(false);

                    Icon deleteIcon = VaadinIcon.TRASH.create();
                    deleteIcon.setColor("grey");

                    Button deleteButton = new Button(deleteIcon);
                    deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
                    deleteButton.setTooltipText(translations.t("button.delete"));

                    deleteButton.addClickListener(e -> {
                        deleteUserConfirmationDialog(userDto);
                    });


                    Icon editIcon = VaadinIcon.EDIT.create();
                    editIcon.setColor("grey");
                    Button editButton = new Button(editIcon);
                    editButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
                    editButton.setTooltipText(translations.t("button.edit"));
                    editButton.addClickListener(e -> {
                        openFormView(userDto);
                    });

                    Icon enableDisableIcon = userDto.isEnabled() ? VaadinIcon.MINUS_CIRCLE.create() : VaadinIcon.CHECK_CIRCLE.create();
                    enableDisableIcon.setColor("grey");
                    Button enableDisableButton = new Button(enableDisableIcon);
                    enableDisableButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
                    enableDisableButton.setTooltipText(userDto.isEnabled() ? translations.t("button.disable") : translations.t("button.enable"));
                    enableDisableButton.addClickListener(e -> {
                        enableDisableDialog(userDto);
                    });

                    Icon passwordResetIcon = VaadinIcon.KEY.create();
                    passwordResetIcon.setColor("grey");
                    Button passwordResetButton = new Button(passwordResetIcon);
                    passwordResetButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
                    passwordResetButton.setTooltipText(userDto.isEnabled() ? translations.t("button.disable") : translations.t("button.enable"));
                    passwordResetButton.addClickListener(e -> {
                        passwordResetDialog(userDto);
                    });

                    actionsLayout.add(deleteButton);
                    actionsLayout.add(editButton);

                    actionsLayout.add(enableDisableButton);
                    actionsLayout.add(passwordResetButton);
                    actionsLayout.setJustifyContentMode(JustifyContentMode.END);
                    return actionsLayout;
                }
                )).setHeader(translations.t("users.columns.actions"))
                .setAutoWidth(true)
                .setFlexGrow(0);

        return userGrid;
    }

    private void passwordResetDialog(UserDto userDto) {
        passwordResetDialog = new PasswordResetDialog(translations, userDto.getUsername(), false, (oldPassword,newPassword) -> {
            userService.resetPassword(userDto, newPassword);
            passwordResetDialog.close();
            paginationView.loadCurrentPageAfterAddition();
            NotificationSupport.showSuccess(translations.t("passwordReset.success"));
        });

        passwordResetDialog.open();
    }

    private void enableDisableDialog(UserDto userDto) {
        ConfirmDialog dialog = new ConfirmDialog();
        boolean enabled = userDto.isEnabled();
        dialog.setHeader(enabled ? translations.t("users.disableUserTitle") : translations.t("users.enableUserTitle"));
        dialog.setText(enabled ? translations.t("users.disableUserMessage", userDto.getUsername())
                : translations.t("users.enableUserMessage", userDto.getUsername()));

        dialog.setCancelable(true);
        dialog.setCancelText(translations.t("button.cancel"));

        dialog.setConfirmText(enabled ? translations.t("button.disable") : translations.t("button.enable"));
        dialog.setConfirmButtonTheme("primary");
        dialog.addConfirmListener(event -> {
            userService.enableDisable(userDto, !enabled);
            paginationView.loadCurrentPageAfterAddition();
            NotificationSupport.showSuccess(enabled ? translations.t("user.disableUserSuccess", userDto.getUsername()) :
                    translations.t("user.enableUserSuccess", userDto.getUsername()));
        });

        dialog.open();
    }


    private void deleteUserConfirmationDialog(UserDto userDto) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(translations.t("users.deleteUserTitle"));
        dialog.setText(
                translations.t("users.deleteUserMessage", userDto.getUsername()));

        dialog.setCancelable(true);
        dialog.setCancelText(translations.t("button.cancel"));

        dialog.setConfirmText(translations.t("button.delete"));
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            userService.delete(userDto);
            paginationView.loadCurrentPageAfterRemove();
            NotificationSupport.showSuccess(translations.t("notification.isDeleted", userDto.getUsername()));
        });

        dialog.open();
    }

    private boolean saveUser() {

        writeBean(currentUser);

        if (currentUser.getId() == null) {
            userService.saveUser(currentUser);
        } else {
            userService.updateUser(currentUser);
        }
        paginationView.loadCurrentPageAfterAddition();
        hideForm();

        NotificationSupport.showSuccess(translations.t("notification.isSaved", translations.t("users.entityName")));
        return true;
    }

    private void writeBean(UserDto bean) {
        bean.setUsername(usernameField.getValue());
        if (bean.getId() == null) {
            bean.setPassword(passwordField.getValue());
        }
        bean.setEmail(emailField.getValue());
        bean.setRoles(rolesCheckBoxGroup.getValue());
    }

    private void readBean(UserDto bean) {
        usernameField.setValue(bean.getUsername() != null ? bean.getUsername() : "");
        emailField.setValue(bean.getEmail() != null ? bean.getEmail() : "");
        passwordField.setValue("");
        if (bean.getId() != null) {
            passwordField.setVisible(false);
        } else {
            passwordField.setVisible(true);
        }
        Set<String> roles = bean.getRoles();
        rolesCheckBoxGroup.deselectAll();
        if (roles != null && !roles.isEmpty()) {
            rolesCheckBoxGroup.select(roles);
        }
    }

    @Override
    public String getPageTitle() {
        return translations.t("menu.users");
    }
}
