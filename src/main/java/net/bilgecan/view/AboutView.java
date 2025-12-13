package net.bilgecan.view;

import net.bilgecan.service.TranslationService;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@RolesAllowed({"USER", "ADMIN"})
@Route(value = "about",  layout = MainLayout.class)
public class AboutView extends VerticalLayout implements HasDynamicTitle {

    private static final String BUY_ME_A_COFFEE_URL =
            "https://buymeacoffee.com/muratoksuzer";

    private TranslationService translations;

    public AboutView(TranslationService translations) {
        this.translations = translations;
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("max-width", "900px");
        getStyle().set("margin", "0 auto");

        String html = loadLocalizedAboutHtml();

        Html content = new Html("<div class='about-content'>" + html + "</div>");
        add(content);

    }

    private String loadLocalizedAboutHtml() {
        Locale locale = UI.getCurrent().getLocale();
        String lang = locale != null ? locale.getLanguage() : "en";

        String fileName;
        switch (lang) {
            case "tr":
                fileName = "static/about/about_tr.html";
                break;
            default:
                fileName = "static/about/about_en.html";
        }

        try {
            ClassPathResource resource = new ClassPathResource(fileName);
            return StreamUtils.copyToString(
                    resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Fallback text if file is missing or unreadable
            return "<h1>About Bilgecan</h1><p>Content not available.</p>";
        }
    }

    @Override
    public String getPageTitle() {
        return translations.t("menu.about");
    }
}
