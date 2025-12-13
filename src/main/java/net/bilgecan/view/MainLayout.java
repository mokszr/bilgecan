package net.bilgecan.view;

import net.bilgecan.config.RoleConstant;
import net.bilgecan.entity.security.User;
import net.bilgecan.service.SecurityService;
import net.bilgecan.service.TranslationService;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.Set;

@CssImport("./styles/main-layout.css")
public class MainLayout extends AppLayout {

    private final transient AuthenticationContext authContext;
    private TranslationService translations;
    private SecurityService securityService;


    public MainLayout(AuthenticationContext authContext, SecurityService securityService, TranslationService translations) {
        this.authContext = authContext;
        this.securityService = securityService;
        this.translations = translations;

        // Top navbar
        HorizontalLayout header = createHeader();
        addToNavbar(header);

        createDrawer();
    }

    private HorizontalLayout createHeader() {
        DrawerToggle toggle = new DrawerToggle();

        Image logo = new Image("images/bilgecan_100x.png", "Bilgecan");
        logo.setHeight("50px"); // <- small toolbar size
        RouterLink logoLink = new RouterLink();
        logoLink.setRoute(ReactiveChatView.class);      //
        logoLink.add(logo);

        H1 title = new H1("bilgecan");
        title.getStyle().setMargin("0");
        RouterLink brand = new RouterLink();
        brand.setRoute(ReactiveChatView.class);      // <- target view
        brand.add(title);

        User currentUser = securityService.getCurrentUser();

        Span loggedUser = new Span(currentUser.getUsername());
        Avatar avatar = new Avatar(currentUser.getUsername());

        // Top right user menu
        Button logoutButton = new Button(translations.t("button.logout"), VaadinIcon.SIGN_OUT.create());
        logoutButton.addClickListener(s -> securityService.logout());

        HorizontalLayout userSection = new HorizontalLayout(loggedUser, avatar, logoutButton);
        userSection.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout header = new HorizontalLayout(toggle, logoLink, brand, userSection);

        header.setWidthFull();

        // Let brand stretch and push userSection to the right
        header.expand(brand);

        // Allow wrapping on small screens
        header.getStyle().set("flex-wrap", "wrap");


        userSection.getStyle().set("margin-left", "auto");

        return header;
    }

    private void createDrawer() {

        boolean hasAdminRole = hasAdminRole();

        SideNav nav = new SideNav();
        nav.setSizeFull();

        SideNavItem dashboardLink = new SideNavItem(translations.t("menu.dashboard"),
                DashboardView.class, VaadinIcon.DASHBOARD.create());

        SideNavItem chatLink = new SideNavItem(
                translations.t("menu.chat"),
                ReactiveChatView.class,
                VaadinIcon.CHAT.create()
        );

        SideNavItem promptsLink = new SideNavItem(
                translations.t("menu.prompts"),
                PromptView.class,
                VaadinIcon.LIST.create()
        );

        SideNavItem tasksLink = new SideNavItem(
                translations.t("menu.tasks"),
                AITaskTemplateView.class,
                VaadinIcon.TASKS.create()
        );

        SideNavItem runsLink = new SideNavItem(
                translations.t("menu.runs"),
                AITaskRunView.class,
                VaadinIcon.PLAY_CIRCLE.create()
        );

        SideNavItem fileProcessingPipelineLink = new SideNavItem(
                translations.t("menu.fileProcessingPipeline"),
                FileProcessingPipelineView.class,
                VaadinIcon.FOLDER_OPEN.create()
        );

        SideNavItem feedRagLink = new SideNavItem(
                translations.t("menu.feedRAG"),
                FeedRAGView.class,
                VaadinIcon.BOOK.create()
        );

        SideNavItem settingsLink = new SideNavItem(
                translations.t("menu.settings"),
                SettingsView.class,
                VaadinIcon.COG.create()
        );

        SideNavItem usersLink = new SideNavItem(
                translations.t("menu.users"),
                UserManagementView.class,
                VaadinIcon.USERS.create()
        );

        SideNavItem workspacesLink = new SideNavItem(
                translations.t("menu.workspaces"),
                WorkspaceView.class,
                VaadinIcon.GRID.create()
        );

        SideNavItem workspaceToolsGroup = new SideNavItem(
                translations.t("menu.workspaceTools")
        );
        workspaceToolsGroup.setPrefixComponent(VaadinIcon.LINES_LIST.create());

        // Child items
        SideNavItem workspacePromptsItem = new SideNavItem(
                translations.t("menu.workspacePrompts"),
                WorkspacePromptView.class,
                VaadinIcon.LIST.create()
        );

        SideNavItem workspaceTasksItem = new SideNavItem(
                translations.t("menu.workspaceTasks"),
                WorkspaceAITaskTemplateView.class,
                VaadinIcon.TASKS.create()
        );

        workspaceToolsGroup.addItem(
                workspacePromptsItem,
                workspaceTasksItem
        );
        workspaceToolsGroup.setExpanded(false);

        SideNavItem aboutLink = new SideNavItem(
                translations.t("menu.about"),
                AboutView.class,
                VaadinIcon.INFO_CIRCLE.create()
        );

        nav.addItem(dashboardLink,
                chatLink,
                promptsLink,
                tasksLink,
                runsLink,
                fileProcessingPipelineLink);

        if (hasAdminRole) {
            nav.addItem(feedRagLink,
                    workspacesLink);
        }

        nav.addItem(workspaceToolsGroup);

        if (hasAdminRole) {
            nav.addItem(usersLink);
        }

        nav.addItem(settingsLink,
                aboutLink
        );

        Scroller scroller = new Scroller(nav);
        scroller.setClassName(LumoUtility.Padding.SMALL);
        addToDrawer(scroller);
    }

    private boolean hasAdminRole() {
        Set<String> currentUserRoles = securityService.getCurrentUserRoles();
        return currentUserRoles.contains(RoleConstant.ROLE_ADMIN);
    }
}