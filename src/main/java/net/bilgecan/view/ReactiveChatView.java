package net.bilgecan.view;

import net.bilgecan.init.OllamaModelRegistry;
import net.bilgecan.service.TranslationService;
import net.bilgecan.support.NotificationSupport;
import net.bilgecan.view.components.MessageInputI18nSupport;
import net.bilgecan.view.components.UploadI18NSupport;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.UploadHandler;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.content.Media;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.MimeType;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Route(value = "chat", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN"})
public class ReactiveChatView extends VerticalLayout implements HasDynamicTitle {

    private final ProgressBar progressBar;
    private final NativeLabel progressBarLabelText;
    private final ComboBox<String> modelComboBox;
    private final Checkbox enableRagCheckbox;
    private final Button abortButton;
    private final Upload upload;
    private TranslationService translations;

    private final MessageList messageList;
    private final MessageInput messageInput;
    private final List<MessageListItem> messages = new ArrayList<>();

    private final ChatClient chatClient;
    private final ChatClient chatClientRagAware;

    private String conversationId = UUID.randomUUID().toString();
    private Disposable disposable;
    private byte[] bytesUploaded;
    private String uploadedContentType;
    private String uploadedFileName;

    public ReactiveChatView(TranslationService translations, ChatModel chatModel, VectorStore vectorStore, OllamaModelRegistry ollamaModelRegistry, ChatMemory chatMemory) {
        this.translations = translations;
        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        this.chatClientRagAware = ChatClient.builder(chatModel)
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore),
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();

        setSizeFull();

        add(new H2(translations.t("chat.title")));

        HorizontalLayout optionsLayout = new HorizontalLayout();
        optionsLayout.setAlignItems(Alignment.BASELINE);
        optionsLayout.setWidthFull();

        modelComboBox = new ComboBox<>(translations.t("prompt.llmModel"));
        List<String> models = ollamaModelRegistry.getModels();
        modelComboBox.setItems(models);
        if (models.contains(chatModel.getDefaultOptions().getModel())) {
            modelComboBox.setValue(chatModel.getDefaultOptions().getModel());
        }
        modelComboBox.setMinWidth("225px");

        optionsLayout.add(modelComboBox);

        enableRagCheckbox = new Checkbox();
        enableRagCheckbox.setLabel(translations.t("prompt.enableRAG"));
        enableRagCheckbox.setTooltipText(translations.t("prompt.enableRAGDescription"));
        enableRagCheckbox.setMinWidth("300px");

        optionsLayout.add(enableRagCheckbox);

        progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setMaxWidth("250px");

        progressBarLabelText = new NativeLabel(translations.t("prompt.thinking"));
        progressBarLabelText.setId("pblabel");
        // Associates the label with the progressbar for screen readers:
        progressBar.getElement().setAttribute("aria-labelledby", "pblabel");

        progressBarLabelText.setVisible(false);
        progressBarLabelText.setMaxWidth("250px");

        VerticalLayout pgBarLayout = new VerticalLayout();
        pgBarLayout.setPadding(false);
        pgBarLayout.setSpacing(false);
        pgBarLayout.add(progressBarLabelText, progressBar);
        pgBarLayout.setMaxWidth("250px");

        optionsLayout.add(pgBarLayout);

        abortButton = new Button(translations.t("button.abort"));
        abortButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        abortButton.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                if (disposable != null) {
                    disposable.dispose();

                    MessageListItem item = new MessageListItem(translations.t("prompt.aborted"), "Bilgecan");
                    item.setUserImage("images/bilgecan_avatar.png");
                    messages.add(item);
                    messageList.setItems(messages);
                    disposable = null;

                    progressBar.setVisible(false);
                    progressBarLabelText.setVisible(false);
                    abortButton.setVisible(false);
                }
            }
        });
        abortButton.setVisible(false);
        optionsLayout.add(abortButton);

        add(optionsLayout);

        messageList = new MessageList();
        messageList.setMarkdown(true);
        messageList.setSizeFull();

        messageInput = new MessageInput();
        messageInput.setI18n(new MessageInputI18nSupport(translations));
        messageInput.setWidthFull();

        // Initial welcome message from Bilgecan
        MessageListItem welcome = new MessageListItem(
                translations.t("chat.welcomeMessage"),
                "Bilgecan"
        );
        welcome.setUserImage("images/bilgecan_avatar.png");

        messages.add(welcome);
        messageList.setItems(messages);

        messageInput.addSubmitListener(event -> handleUserMessage(event.getValue()));

        InMemoryUploadHandler inMemoryUploadHandler = UploadHandler.inMemory((uploadMetadata, bytesUploaded) -> {
            this.bytesUploaded = bytesUploaded;
            this.uploadedContentType = uploadMetadata.contentType();
            this.uploadedFileName = uploadMetadata.fileName();
            System.out.println(uploadedContentType);
        });

        upload = new Upload(inMemoryUploadHandler);
        upload.setMaxFiles(1);
        int maxFileSizeInBytes = 100 * 1024 * 1024; // 100MB
        upload.setMaxFileSize(maxFileSizeInBytes);
        upload.addFileRejectedListener(event -> {
            String errorMessage = event.getErrorMessage();

            NotificationSupport.showError(errorMessage);

        });
        upload.getStyle().setOverflow(Style.Overflow.AUTO);

        upload.setMinWidth("300px");
        upload.setMinHeight("100px");
        UploadI18NSupport uploadI18NSupport = new UploadI18NSupport(translations);
        uploadI18NSupport.getAddFiles().setOne(translations.t("chat.addPhotosFiles"));
        upload.setI18n(uploadI18NSupport);

        add(messageList, upload, messageInput);
        setFlexGrow(1, messageList);
    }

    private void handleUserMessage(String text) {
        if (text == null || text.isBlank()) {
            return;
        }

        Media media = null;

        if (StringUtils.isNotBlank(this.uploadedContentType)) {
            MimeType mimeType = MimeType.valueOf(uploadedContentType);
            if (mimeType == null) {
                NotificationSupport.showError(translations.t("chat.unknownFileType") + this.uploadedContentType);
            } else {
                media = Media.builder()
                        .mimeType(mimeType)
                        .data(new ByteArrayResource(bytesUploaded)).build();
            }

        }

        //TODO make images appear in user message
        MessageListItem userItem = new MessageListItem(
                text.trim() + (uploadedFileName != null ? " " + translations.t("chat.attachedFile") + uploadedFileName : ""),
                translations.t("chat.you")
        );
        userItem.setUserColorIndex(2);

        messages.add(userItem);

        MessageListItem botItem = new MessageListItem(
                translations.t("prompt.thinking"),
                "Bilgecan"
        );

        botItem.setUserImage("images/bilgecan_avatar.png");
        messages.add(botItem);

        messageList.setItems(messages);

        callChatClient(text, botItem, media);
    }

    private void callChatClient(String userMessage, MessageListItem botItem, Media media) {
        OllamaOptions.Builder optionsBuilder = OllamaOptions.builder()
                .model(modelComboBox.getValue());

        ChatClient chatClientSelected = selectChatClient(enableRagCheckbox.getValue());
        Flux<ChatResponse> stream = chatClientSelected.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .options(optionsBuilder.build())
                .user(u -> {
                    u.text(userMessage);
                    if (media != null) {
                        u.media(media);
                    }
                })
                .stream()
                .chatResponse();

        this.bytesUploaded = null;
        this.uploadedContentType = null;
        this.uploadedFileName = null;
        upload.clearFileList();

        progressBarLabelText.setText(translations.t("prompt.thinking"));
        progressBar.setVisible(true);
        progressBarLabelText.setVisible(true);
        abortButton.setVisible(true);

        StringBuilder sb = new StringBuilder();

        disposable = stream.subscribe(
                chunk -> {
                    String text = extractText(chunk);
                    messageList.getUI().ifPresent(ui -> ui.access(() -> {
                        botItem.setText(sb.append(text).toString());
                        progressBarLabelText.setText(translations.t("prompt.generating"));
                        ui.push();
                    }));
                },
                err -> {
                    messageList.getUI().ifPresent(ui -> ui.access(() -> {
                        botItem.setText("Error: " + err.getMessage());
                        NotificationSupport.showError("error " + err.getMessage());
                        progressBar.setVisible(false);
                        progressBarLabelText.setVisible(false);
                        abortButton.setVisible(false);
                        ui.push();
                    }));

                },
                () -> {
                    messageList.getUI().ifPresent(ui -> ui.access(() -> {
                        NotificationSupport.showInfo(translations.t("prompt.done"));
                        progressBar.setVisible(false);
                        progressBarLabelText.setVisible(false);
                        abortButton.setVisible(false);
                        ui.push();
                    }));

                }
        );
    }

    private String extractText(ChatResponse r) {
        try {
            Generation result = r.getResult();
            if (result != null && result.getOutput() != null) {
                var content = result.getOutput().getText();
                if (content != null) return content;
            }
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }

        return "";
    }

    private ChatClient selectChatClient(Boolean enableRag) {
        if (enableRag) {
            return chatClientRagAware;
        }
        return chatClient;
    }

    @Override
    public String getPageTitle() {
        return translations.t("menu.chat");
    }
}
