package net.bilgecan.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.Scroller;
import net.bilgecan.dto.PromptDto;
import net.bilgecan.init.OllamaModelRegistry;
import net.bilgecan.service.PromptService;
import net.bilgecan.service.TranslationService;
import net.bilgecan.support.NotificationSupport;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.jetbrains.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.VectorStore;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Route(value = "promptExec", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN"})
@PageTitle("Prompt Execution")
public class PromptExecutionView extends VerticalLayout implements HasUrlParameter<Long> {

    private final TranslationService translations;
    private final OllamaModelRegistry ollamaModelRegistry;
    private final ProgressBar progressBar;
    private final NativeLabel progressBarLabelText;
    private final Button executeButton;
    private final Button abortButton;
    private final Button editButton;

    private PromptService promptService;
    private PromptDto currentPrompt;
    private Span promptName;
    private TextArea promptInputField;
    private Card card;
    private final Markdown promptParagraph;

    private final MessageList messageList;

    private final ChatClient chatClient;
    private final ChatClient chatClientRagAware;
    private Flux<ChatResponse> chatResponse;
    private Disposable disposable;


    public PromptExecutionView(PromptService promptService, TranslationService translations, ChatModel chatModel, VectorStore vectorStore, OllamaModelRegistry ollamaModelRegistry) {
        this.promptService = promptService;
        this.translations = translations;
        this.ollamaModelRegistry = ollamaModelRegistry;
        this.chatClient = ChatClient.builder(chatModel).build();
        this.chatClientRagAware = ChatClient.builder(chatModel).defaultAdvisors(new QuestionAnswerAdvisor(vectorStore)).build();
        setSizeFull();

        add(new H2(translations.t("prompt.executePrompt")));

        promptName = new Span("");
        promptInputField = new TextArea();
        promptInputField.setWidthFull();
        promptInputField.setVisible(false);
        promptInputField.setMaxHeight("450px");

        promptParagraph = new Markdown();
        promptParagraph.setWidthFull();
        promptParagraph.setMaxHeight("450px");
        promptParagraph.getStyle().set("text-overflow", "ellipsis");
        promptParagraph.getStyle().set("overflow-wrap", "break-word");
        promptParagraph.getStyle().set("white-space", "normal");
        promptParagraph.getStyle().set("overflow", "auto");

        card = new Card();
        card.setWidthFull();
        card.setMaxHeight("500px");
        card.add(promptParagraph);

        add(promptName);

        HorizontalLayout optionsLayout = new HorizontalLayout();
        optionsLayout.setAlignItems(Alignment.BASELINE);

        ComboBox<String> modelComboBox = new ComboBox<>(translations.t("prompt.llmModel"));
        List<String> models = ollamaModelRegistry.getModels();
        modelComboBox.setItems(models);
        if (models.contains(chatModel.getDefaultOptions().getModel())) {
            modelComboBox.setValue(chatModel.getDefaultOptions().getModel());
        }

        optionsLayout.add(modelComboBox);

        Checkbox enableRagCheckbox = new Checkbox();
        enableRagCheckbox.setLabel(translations.t("prompt.enableRAG"));
        enableRagCheckbox.setTooltipText(translations.t("prompt.enableRAGDescription"));

        optionsLayout.add(enableRagCheckbox);

        HorizontalLayout toolbarLayout = new HorizontalLayout();

        executeButton = new Button(translations.t("button.execute"));
        executeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        executeButton.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                OllamaOptions.Builder optionsBuilder = OllamaOptions.builder()
                        .model(modelComboBox.getValue());

                MessageListItem item = new MessageListItem(translations.t("prompt.thinking"), translations.t("prompt.aiResponse"));
                item.setUserAbbreviation(translations.t("prompt.userNameAI"));

                messageList.setItems(Arrays.asList(item));

                promptParagraph.setContent(promptInputField.getValue());
                card.setVisible(true);
                promptInputField.setVisible(false);

                ChatClient chatClientSelected = selectChatClient(enableRagCheckbox.getValue());

                chatResponse = chatClientSelected.prompt()
                        .options(optionsBuilder.build())
                        .user(promptInputField.getValue())
                        .stream()
                        .chatResponse();

                progressBarLabelText.setText(translations.t("prompt.thinking"));
                progressBar.setVisible(true);
                progressBarLabelText.setVisible(true);
                streamToMessage(item, chatResponse);

            }
        });

        abortButton = new Button(translations.t("button.abort"));
        abortButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        abortButton.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                if (disposable != null) {
                    disposable.dispose();

                    MessageListItem item = new MessageListItem(translations.t("prompt.aborted"), translations.t("prompt.aiResponse"));
                    item.setUserAbbreviation(translations.t("prompt.userNameAI"));
                    messageList.addItem(item);
                    disposable = null;

                    progressBar.setVisible(false);
                    progressBarLabelText.setVisible(false);
                }
            }
        });

        editButton = new Button(translations.t("button.edit"));
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                card.setVisible(false);
                promptInputField.setVisible(true);

            }
        });

        toolbarLayout.add(executeButton);
        toolbarLayout.add(abortButton);
        toolbarLayout.add(editButton);

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
        toolbarLayout.add(pgBarLayout);

        messageList = new MessageList();
        messageList.setMarkdown(true);
        messageList.setSizeFull();

        Scroller scroller = new Scroller(new Div(promptInputField, card, optionsLayout, toolbarLayout, messageList));
        scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
        scroller.setWidthFull();
        add(scroller);

    }

    private ChatClient selectChatClient(Boolean enableRag) {
        if (enableRag) {
            return chatClientRagAware;
        }
        return chatClient;
    }

    private void streamToMessage(MessageListItem item, Flux<ChatResponse> stream) {
        StringBuilder sb = new StringBuilder();

        disposable = stream.subscribe(
                chunk -> {
                    String text = extractText(chunk);
                    messageList.getUI().ifPresent(ui -> ui.access(() -> {
                        item.setText(sb.append(text).toString());
                        progressBarLabelText.setText(translations.t("prompt.generating"));
                        ui.push();
                    }));
                    //System.out.println("chunk: " + chunk);
                },
                err -> {
                    messageList.getUI().ifPresent(ui -> ui.access(() -> {
                        item.setText("Error: " + err.getMessage());
                        messageList.setItems(Arrays.asList(item));
                        NotificationSupport.showError("error " + err.getMessage());
                        progressBar.setVisible(false);
                        progressBarLabelText.setVisible(false);
                        ui.push();
                    }));

                },
                () -> {
                    messageList.getUI().ifPresent(ui -> ui.access(() -> {
                        NotificationSupport.showInfo(translations.t("prompt.done"));
                        progressBar.setVisible(false);
                        progressBarLabelText.setVisible(false);
                        ui.push();
                    }));

                }
        );
    }

    private static String extractText(ChatResponse r) {
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

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long promptId) {
        Long wsId = extractWorkspaceId(beforeEvent);

        currentPrompt = promptService.getPrompt(promptId, wsId);
        if (currentPrompt != null) {
            promptName.setText(currentPrompt.getName());
            promptName.setTitle(currentPrompt.getName());

            promptInputField.setValue(currentPrompt.getInput());
            promptParagraph.setContent(currentPrompt.getInput());

        } else {
            promptName.setText(translations.t("prompt.promptNotFound"));
            executeButton.setEnabled(false);
            abortButton.setEnabled(false);
            editButton.setEnabled(false);
        }
    }

    @Nullable
    private static Long extractWorkspaceId(BeforeEvent beforeEvent) {
        Optional<String> ws = beforeEvent.getLocation().getQueryParameters().getSingleParameter("ws");
        Long wsId = null;
        try {
            if(ws.isPresent()) {
                wsId = Long.valueOf(ws.get());
            }
        } catch (NumberFormatException e) {
            wsId = null;
        }
        return wsId;
    }
}
