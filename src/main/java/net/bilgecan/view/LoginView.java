package net.bilgecan.view;

import net.bilgecan.service.TranslationService;
import net.bilgecan.view.components.LoginFormI18nSupport;
import net.bilgecan.view.subviews.CallToActionView;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Login")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm;

    public LoginView(TranslationService translations) {
        Image image = new Image("images/bilgecan_logo.png", "bilgecan");
        image.setMaxHeight(25f, Unit.PERCENTAGE);

        loginForm = new LoginForm();
        loginForm.setAction("login");
        LoginFormI18nSupport i18n = new LoginFormI18nSupport(translations);
        i18n.setAdditionalInformation("");
        loginForm.setI18n(i18n);
        loginForm.setForgotPasswordButtonVisible(false);


        CallToActionView callToActionView = new CallToActionView(translations);

        add(image);
        add(loginForm);
        add(callToActionView);
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        boolean hasError = event.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error");

        if (hasError) {
            loginForm.setError(true);
        }
    }
}