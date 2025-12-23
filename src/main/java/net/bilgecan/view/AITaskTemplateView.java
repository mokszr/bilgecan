package net.bilgecan.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.bilgecan.dto.AITaskDto;
import net.bilgecan.init.OllamaModelRegistry;
import net.bilgecan.service.AITaskRunService;
import net.bilgecan.service.AITaskTemplateService;
import net.bilgecan.service.TranslationService;
import net.bilgecan.support.NotificationSupport;
import net.bilgecan.view.components.PaginationView;
import net.bilgecan.view.components.SearchBarView;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

@Route(value = "aitask", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN"})
public class AITaskTemplateView extends VerticalLayout implements HasDynamicTitle {

    private final SearchBarView searchBarView;
    private AITaskTemplateService aiTaskTemplateService;
    private AITaskRunService aiTaskRunService;
    private final TranslationService translations;
    private Binder<AITaskDto> binder;
    private PaginationView paginationView;
    private OllamaModelRegistry ollamaModelRegistry;

    private AITaskDto currentAITask;
    private TextField name;
    private TextArea userPromptField;
    private Button saveButton;
    private Button cancelButton;
    private Grid<AITaskDto> aiTaskGrid;
    private VerticalLayout formLayout;
    private TextArea systemPromptField;
    private TextArea jsonSchemaField;

    public AITaskTemplateView(AITaskTemplateService aiTaskTemplateService, AITaskRunService aiTaskRunService, OllamaModelRegistry ollamaModelRegistry,
                              TranslationService translations) {
        this.aiTaskTemplateService = aiTaskTemplateService;
        this.aiTaskRunService = aiTaskRunService;
        this.translations = translations;
        this.ollamaModelRegistry = ollamaModelRegistry;
        setSizeFull();

        add(new H2(translations.t("aiTaskTemplate.yourAITaskTemplates")));

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

        Grid<AITaskDto> grid = createGrid();

        add(grid);

        paginationView = new PaginationView() {

            @Override
            protected Page loadAndGetPage(Pageable pageable, String searchTerm) {
                Page<AITaskDto> termPage = aiTaskTemplateService.findPaginated(pageable, searchTerm);
                // Update the grid with the current page data
                aiTaskGrid.setItems(termPage.getContent());
                return termPage;
            }
        };
        paginationView.setCurrentPageKey("currentPageAITaskTemplate");
        add(paginationView);
        paginationView.loadPage(paginationView.getCurrentPageInSession());
        hideForm();
    }

    private void createForm() {
        formLayout = new VerticalLayout();
        formLayout.setSizeFull();
        formLayout.setVisible(false);

        name = new TextField(translations.t("aiTaskTemplate.name"));
        name.setWidthFull();

        binder = new Binder<>(AITaskDto.class);
        binder.forField(name)
                .withValidator(name -> StringUtils.isNotBlank(name), translations.t("validation.fieldCannotBeBlank", translations.t("aiTaskTemplate.name")))
                .bind(
                        AITaskDto::getName,
                        AITaskDto::setName);

        userPromptField = new TextArea(translations.t("aiTaskTemplate.userPrompt"));
        userPromptField.setMaxRows(6);
        userPromptField.setPlaceholder(translations.t("aiTaskTemplate.userPromptPlaceholder"));
        userPromptField.setWidthFull();

        binder.forField(userPromptField).bind(
                AITaskDto::getPrompt,
                AITaskDto::setPrompt);

        systemPromptField = new TextArea(translations.t("aiTaskTemplate.systemPrompt"));
        systemPromptField.setMaxRows(6);
        systemPromptField.setPlaceholder(translations.t("aiTaskTemplate.systemPromptPlaceholder"));
        systemPromptField.setWidthFull();

        binder.forField(systemPromptField).bind(
                AITaskDto::getSystemPrompt,
                AITaskDto::setSystemPrompt);

        jsonSchemaField = new TextArea(translations.t("aiTaskTemplate.jsonSchema"));
        jsonSchemaField.setMaxRows(6);
        jsonSchemaField.setPlaceholder(translations.t("aiTaskTemplate.jsonSchemaPlaceholder"));
        jsonSchemaField.setWidthFull();


        binder.forField(jsonSchemaField)
                .withValidator(jsonSchemaValue -> {
                    if (StringUtils.isBlank(jsonSchemaValue)) {
                        return true;
                    }
                    try {
                        new ObjectMapper().readValue(jsonSchemaValue, Map.class);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }

                }, translations.t("aiTaskTemplate.jsonSchemaInvalid"))
                .bind(
                        AITaskDto::getJsonSchema,
                        AITaskDto::setJsonSchema);

        ComboBox<String> modelComboBox = new ComboBox<>(translations.t("aiTaskTemplate.llmModel"));
        modelComboBox.setItems(ollamaModelRegistry.getModels());
        modelComboBox.setWidthFull();

        binder.forField(modelComboBox)
                .withValidator(StringUtils::isNotBlank, translations.t("validation.fieldCannotBeBlank", translations.t("aiTaskTemplate.llmModel")))
                .bind(
                        AITaskDto::getModelRoute,
                        AITaskDto::setModelRoute
                );

        Checkbox enableRagCheckBox = new Checkbox(translations.t("aiTaskTemplate.enableRag"));
        binder.forField(enableRagCheckBox)
                .bind(AITaskDto::isEnableRag,
                        AITaskDto::setEnableRag);

        saveButton = new Button(translations.t("button.save"));
        cancelButton = new Button(translations.t("button.cancel"));

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        cancelButton.addClickShortcut(Key.ESCAPE);
        cancelButton.addClickListener(ev -> {
            hideForm();
        });

        saveButton.addClickListener(ev -> {
            boolean saved = saveAITask();
            if (saved) {
                hideForm();
            }
        });

        HorizontalLayout buttons = new HorizontalLayout(cancelButton, saveButton);
        buttons.setJustifyContentMode(JustifyContentMode.END);
        buttons.setWidthFull();

        formLayout.add(name);
        formLayout.add(userPromptField);
        formLayout.add(systemPromptField);
        formLayout.add(jsonSchemaField);
        formLayout.add(modelComboBox);
        formLayout.add(enableRagCheckBox);

        formLayout.add(buttons);

        add(formLayout);
    }

    private HorizontalLayout createToolbar() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();

        Button addNewButton = new Button(translations.t("button.add"));

        addNewButton.addClickListener(ev -> openFormView(new AITaskDto()));

        addNewButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        horizontalLayout.add(addNewButton);
        return horizontalLayout;
    }

    private void openFormView(AITaskDto aAITaskDto) {
        currentAITask = aAITaskDto;
        binder.readBean(currentAITask);

        formLayout.setVisible(true);
        searchBarView.setVisible(false);
        aiTaskGrid.setVisible(false);
        paginationView.setVisible(false);
    }

    private void hideForm() {
        formLayout.setVisible(false);
        searchBarView.setVisible(true);
        aiTaskGrid.setVisible(true);
        paginationView.setVisible(true);
    }

    private Grid<AITaskDto> createGrid() {
        aiTaskGrid = new Grid<>(AITaskDto.class, false);
        aiTaskGrid.setSizeFull();
        aiTaskGrid.setEmptyStateText(translations.t("general.noItems"));

        aiTaskGrid.addColumn(new ComponentRenderer<>(aiTask -> {
            VerticalLayout hl = new VerticalLayout();
            hl.setSizeFull();
            H4 h4 = new H4(aiTask.getName());
            h4.setSizeFull();
            h4.getStyle().set("text-overflow", "ellipsis");
            h4.getStyle().set("overflow-wrap", "break-word");
            h4.getStyle().set("white-space", "normal");

            hl.add(h4);

            Span promptBadge = new Span(aiTask.getPrompt());
            promptBadge.getElement().getThemeList().add("badge pill small contrast");

            String counterLabel = translations.t("aiTaskTemplate.userPrompt", aiTask.getPrompt());
            promptBadge.getElement().setAttribute("aria-label", counterLabel);
            promptBadge.getElement().setAttribute("title", counterLabel);

            hl.add(promptBadge);

            return hl;
        })).setFlexGrow(1);

        aiTaskGrid.addColumn(new ComponentRenderer<>(aiTaskDto -> {
            HorizontalLayout actionsLayout = new HorizontalLayout();

            Icon deleteIcon = VaadinIcon.TRASH.create();
            deleteIcon.setColor("gray");
            Button deleteButton = new Button(deleteIcon);
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            deleteButton.setTooltipText(translations.t("button.delete"));

            deleteButton.addClickListener(e -> {
                deleteAITaskConfirmationDialog(aiTaskDto);
            });

            Icon editIcon = VaadinIcon.EDIT.create();
            editIcon.setColor("gray");
            Button editButton = new Button(editIcon);
            editButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            editButton.setTooltipText(translations.t("button.edit"));
            editButton.addClickListener(e -> {
                openFormView(aiTaskDto);
            });

            Icon runIcon = VaadinIcon.PLAY.create();

            Button runButton = new Button(runIcon);
            runButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            runButton.setTooltipText(translations.t("button.run"));
            runButton.addClickListener(e -> {
                runTask(aiTaskDto);
            });

            actionsLayout.add(deleteButton);
            actionsLayout.add(editButton);
            actionsLayout.add(runButton);
            actionsLayout.setJustifyContentMode(JustifyContentMode.END);
            return actionsLayout;
        }
        ));

        return aiTaskGrid;
    }

    private void runTask(AITaskDto aiTaskDto) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(translations.t("aiTaskTemplate.runAITaskTitle"));
        dialog.setText(
                translations.t("aiTaskTemplate.runAITaskMessage", aiTaskDto.getName()));

        dialog.setCancelable(true);
        dialog.setCancelText(translations.t("button.cancel"));

        dialog.setConfirmText(translations.t("button.run"));
        dialog.setConfirmButtonTheme("primary");
        dialog.addConfirmListener(event -> {
            aiTaskRunService.saveTaskRun(aiTaskDto.getId());

            NotificationSupport.showSuccess(translations.t("aiTaskTemplate.aiTaskQueuedToRun", aiTaskDto.getName()));
        });

        dialog.open();
    }

    private void deleteAITaskConfirmationDialog(AITaskDto aiTask) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(translations.t("aiTaskTemplate.deleteAITaskTemplateTitle"));
        dialog.setText(
                translations.t("aiTaskTemplate.deleteAITaskTemplateMessage", aiTask.getName()));

        dialog.setCancelable(true);
        dialog.setCancelText(translations.t("button.cancel"));

        dialog.setConfirmText(translations.t("button.delete"));
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            aiTaskTemplateService.delete(aiTask);
            paginationView.loadCurrentPageAfterRemove();
            NotificationSupport.showSuccess(translations.t("notification.isDeleted", aiTask.getName()));
        });

        dialog.open();
    }

    private boolean saveAITask() {
        try {
            binder.writeBean(currentAITask);

            aiTaskTemplateService.saveAITask(currentAITask);
            paginationView.loadCurrentPageAfterAddition();
            hideForm();

            NotificationSupport.showSuccess(translations.t("notification.isSaved", translations.t("aiTaskTemplate.entityName")));
            return true;
        } catch (ValidationException ve) {
            ve.getValidationErrors().stream().forEach(v -> NotificationSupport.showError(v.getErrorMessage()));
            return false;
        }
    }

    @Override
    public String getPageTitle() {
        return translations.t("menu.tasks");
    }
}
