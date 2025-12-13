package net.bilgecan.service;

import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class TranslationService {

    @Autowired
    private I18NProvider i18nProvider;

    public String t(String key, Object ...parameters) {
        return i18nProvider.getTranslation(key, VaadinSession.getCurrent().getLocale(), parameters);
    }

    public String t(String key, Locale locale, Object ...parameters) {
        return i18nProvider.getTranslation(key, locale, parameters);
    }
}
