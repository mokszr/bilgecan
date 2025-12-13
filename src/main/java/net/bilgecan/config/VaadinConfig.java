package net.bilgecan.config;

import net.bilgecan.entity.security.User;
import net.bilgecan.service.TranslationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.i18n.DefaultI18NProvider;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Locale;

@Configuration
public class VaadinConfig {

    @Autowired
    private ApplicationContext applicationContext;

    // Define a Bean for VaadinServiceInitListener to hook into UI initialization
    @Bean
    public VaadinServiceInitListener vaadinServiceInitListener() {
        return new VaadinServiceInitListener() {
            @Override
            public void serviceInit(ServiceInitEvent event) {
                // Add a UI initialization listener during the Vaadin service initialization
                event.getSource().addUIInitListener(uiInitEvent -> {
                    // Set a global error handler for each UI when it's initialized
                    UI ui = uiInitEvent.getUI();
                    VaadinSession session = ui.getSession();
                    TranslationService translations = applicationContext.getBean(TranslationService.class);
                    session.setErrorHandler(new CustomErrorHandler(translations));

                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                        Object principal = auth.getPrincipal();
                        if (principal instanceof UserDetails) {
                            User user = (User) auth.getPrincipal();
                            Locale preferred = user.getLocale();
                            if (preferred != null) {
                                session.setLocale(preferred);
                            }
                        }
                    }
                });
            }
        };
    }

    @Bean
    public I18NProvider i18nProvider() {
        return new DefaultI18NProvider(List.of(Locale.ENGLISH, new Locale("tr", "TR")));
    }

}
