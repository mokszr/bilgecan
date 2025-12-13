package net.bilgecan.view.components;

import net.bilgecan.service.TranslationService;
import com.vaadin.flow.component.messages.MessageInputI18n;

public class MessageInputI18nSupport extends MessageInputI18n {

    public MessageInputI18nSupport(TranslationService translations) {
        setMessage(translations.t("messageInputi18n.message"));
        setSend(translations.t("messageInputi18n.send"));
    }
}
