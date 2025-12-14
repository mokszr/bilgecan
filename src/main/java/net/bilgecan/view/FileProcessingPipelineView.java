package net.bilgecan.view;

import net.bilgecan.dto.AITaskDto;
import net.bilgecan.dto.FileProcessingPipelineDto;
import net.bilgecan.dto.InputSourceDto;
import net.bilgecan.dto.OutputTargetDto;
import net.bilgecan.entity.InputSourceType;
import net.bilgecan.entity.OutputTargetType;
import net.bilgecan.pipeline.fileprocessing.InputSourceResolverService;
import net.bilgecan.pipeline.fileprocessing.OutputWriterService;
import net.bilgecan.service.AITaskTemplateService;
import net.bilgecan.service.FileProcessingPipelineService;
import net.bilgecan.service.TranslationService;
import net.bilgecan.support.NotificationSupport;
import net.bilgecan.view.components.PaginationView;
import net.bilgecan.view.components.SearchBarView;
import net.bilgecan.view.subviews.ChooseAITaskDialog;
import net.bilgecan.view.subviews.FileSystemInputSourceParamsView;
import net.bilgecan.view.subviews.FileSystemOutputTargetParamsView;
import net.bilgecan.view.subviews.ToBeImplementedView;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Objects;

@Route(value = "filepipeline", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN"})
public class FileProcessingPipelineView extends VerticalLayout implements HasDynamicTitle {

    private final TranslationService translations;
    private final SearchBarView searchBarView;
    private FileProcessingPipelineService fileProcessingPipelineService;
    private InputSourceResolverService inputSourceResolverService;
    private OutputWriterService outputWriterService;
    private AITaskTemplateService aiTaskTemplateService;

    private Binder<FileProcessingPipelineDto> binder;
    private PaginationView paginationView;

    private FileProcessingPipelineDto currentFilePipeline;
    private TextField name;
    private Button saveButton;
    private Button cancelButton;
    private Grid<FileProcessingPipelineDto> filePipelineGrid;
    private VerticalLayout formLayout;
    private VerticalLayout inputSourceLayout;
    private VerticalLayout outputTargetLayout;

    private FileSystemInputSourceParamsView fileSystemInputSourceParamsView;
    private FileSystemOutputTargetParamsView fileSystemOutputTargetParamsView;
    private H4 selectedTaskH4;

    public FileProcessingPipelineView(FileProcessingPipelineService fileProcessingPipelineService,
                                      InputSourceResolverService inputSourceResolverService,
                                      OutputWriterService outputWriterService,
                                      AITaskTemplateService aiTaskTemplateService,
                                      TranslationService translations) {
        this.fileProcessingPipelineService = fileProcessingPipelineService;
        this.inputSourceResolverService = inputSourceResolverService;
        this.outputWriterService = outputWriterService;
        this.aiTaskTemplateService = aiTaskTemplateService;
        this.translations = translations;

        setSizeFull();

        add(new H2(translations.t("fileProcessingPipeline.yourFileProcessingPipelines")));

        HorizontalLayout toolbar = createToolbar();
        add(toolbar);

        VerticalLayout contentRoot = new VerticalLayout();
        contentRoot.setSizeFull();
        contentRoot.setPadding(false);
        contentRoot.setSpacing(false);

        VerticalLayout formLayout = createForm();
        contentRoot.add(formLayout);
        contentRoot.setFlexGrow(1, formLayout);

        searchBarView = new SearchBarView(translations) {

            @Override
            public void searchClicked(String searchValue) {
                paginationView.setSearchTerm(searchValue);
                long foundCount = paginationView.loadPage(0);
                NotificationSupport.showInfo(translations.t("general.search.foundNotification", foundCount));
            }
        };

        searchBarView.setJustifyContentMode(JustifyContentMode.END);

        contentRoot.add(searchBarView);

        Grid<FileProcessingPipelineDto> grid = createGrid();

        contentRoot.add(grid);

        paginationView = new PaginationView() {

            @Override
            protected Page<FileProcessingPipelineDto> loadAndGetPage(Pageable pageable, String searchTerm) {
                Page<FileProcessingPipelineDto> termPage = fileProcessingPipelineService.findPaginated(pageable, searchTerm);
                // Update the grid with the current page data
                filePipelineGrid.setItems(termPage.getContent());
                return termPage;
            }
        };
        paginationView.setCurrentPageKey("currentPageFileProcessingPipeline");
        contentRoot.add(paginationView);
        paginationView.loadPage(paginationView.getCurrentPageInSession());
        add(contentRoot);

        hideForm();
    }


    private VerticalLayout createForm() {
        formLayout = new VerticalLayout();
        formLayout.setSizeFull();
        formLayout.setVisible(false);

        name = new TextField(translations.t("fileProcessingPipeline.name"));
        name.setWidthFull();

        binder = new Binder<>(FileProcessingPipelineDto.class);
        binder.forField(name)
                // Finalize by doing the actual binding
                // to the Person class
                .withValidator(StringUtils::isNotBlank, translations.t("validation.fieldCannotBeBlank", translations.t("fileProcessingPipeline.name")))
                .bind(
                        // Callback that loads the title
                        // from a person instance
                        FileProcessingPipelineDto::getName,
                        // Callback that saves the title
                        // in a person instance
                        FileProcessingPipelineDto::setName);

        ComboBox<InputSourceType> inputSourceTypeComboBox = new ComboBox<>(translations.t("fileProcessingPipeline.inputSourceType"));
        inputSourceTypeComboBox.setItems(InputSourceType.values());
        inputSourceTypeComboBox.setItemLabelGenerator(this::translateInputSourceType);
        inputSourceTypeComboBox.setWidthFull();

        inputSourceTypeComboBox.addValueChangeListener((HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<ComboBox<InputSourceType>, InputSourceType>>)
                event -> {
                    InputSourceType selectedSourceType = event.getValue();
                    if (InputSourceType.FILE_SYSTEM.equals(selectedSourceType)) {
                        fileSystemInputSourceParamsView = new FileSystemInputSourceParamsView(inputSourceResolverService, translations);
                        inputSourceLayout.removeAll();
                        inputSourceLayout.add(fileSystemInputSourceParamsView);
                        fileSystemInputSourceParamsView.readBean(currentFilePipeline.getInputSource());
                    } else {
                        ToBeImplementedView toBeImplementedView = new ToBeImplementedView(translations);
                        inputSourceLayout.removeAll();
                        inputSourceLayout.add(toBeImplementedView);
                    }
                });

        binder.forField(inputSourceTypeComboBox)
                .withValidator(Objects::nonNull, translations.t("validation.fieldCannotBeBlank", translations.t("fileProcessingPipeline.inputSourceType")))
                .bind(
                        fp -> fp.getInputSource() != null ? fp.getInputSource().getType() : null,   // getter
                        (fp, value) -> {                                                          // setter
                            if (fp.getInputSource() == null) {
                                fp.setInputSource(new InputSourceDto());
                            }
                            fp.getInputSource().setType(value);
                        }
                );

        inputSourceLayout = new VerticalLayout();
        inputSourceLayout.setSizeFull();

        HorizontalLayout taskSelectLayout = new HorizontalLayout();
        taskSelectLayout.setPadding(false);
        taskSelectLayout.setMargin(false);
        taskSelectLayout.setAlignItems(Alignment.END);

        selectedTaskH4 = new H4(translations.t("fileProcessingPipeline.aiTaskToRun"));
        Button chooseTask = new Button(translations.t("fileProcessingPipeline.chooseTask"));
        chooseTask.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        chooseTask.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                openTaskSelectDialog();
            }
        });

        taskSelectLayout.add(selectedTaskH4);
        taskSelectLayout.add(chooseTask);

        ComboBox<OutputTargetType> outputTargetTypeComboBox = new ComboBox<>(translations.t("fileProcessingPipeline.outputTargetType"));
        outputTargetTypeComboBox.setItems(OutputTargetType.values());
        outputTargetTypeComboBox.setItemLabelGenerator(this::translateOutputTargetType);
        outputTargetTypeComboBox.setWidthFull();

        outputTargetTypeComboBox.addValueChangeListener((HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<ComboBox<OutputTargetType>, OutputTargetType>>)
                event -> {
                    OutputTargetType selectedSourceType = event.getValue();
                    if (OutputTargetType.FILE_SYSTEM.equals(selectedSourceType)) {
                        fileSystemOutputTargetParamsView = new FileSystemOutputTargetParamsView(outputWriterService, translations);
                        outputTargetLayout.removeAll();
                        outputTargetLayout.add(fileSystemOutputTargetParamsView);
                        fileSystemOutputTargetParamsView.readBean(currentFilePipeline.getOutputTarget());
                    } else {
                        ToBeImplementedView toBeImplementedView = new ToBeImplementedView(translations);
                        outputTargetLayout.removeAll();
                        outputTargetLayout.add(toBeImplementedView);
                    }
                });

        binder.forField(outputTargetTypeComboBox)
                .withValidator(Objects::nonNull, translations.t("validation.fieldCannotBeBlank", translations.t("fileProcessingPipeline.outputTargetType")))
                .bind(
                        fp -> fp.getOutputTarget() != null ? fp.getOutputTarget().getType() : null,   // getter
                        (fp, value) -> {                                                          // setter
                            if (fp.getOutputTarget() == null) {
                                fp.setOutputTarget(new OutputTargetDto());
                            }
                            fp.getOutputTarget().setType(value);
                        }
                );

        outputTargetLayout = new VerticalLayout();
        outputTargetLayout.setSizeFull();

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
        formLayout.add(inputSourceTypeComboBox);
        formLayout.add(inputSourceLayout);
        formLayout.add(taskSelectLayout);
        formLayout.add(outputTargetTypeComboBox);
        formLayout.add(outputTargetLayout);

        formLayout.add(buttons);

        return formLayout;
    }

    private void openTaskSelectDialog() {
        ChooseAITaskDialog chooseAITaskDialog = new ChooseAITaskDialog(aiTaskTemplateService, this::aiTaskSelected, translations);
        chooseAITaskDialog.open();
    }

    public void aiTaskSelected(AITaskDto aiTaskDto) {
        selectedTaskH4.setText(translations.t("fileProcessingPipeline.aiTaskToRun") + " " + aiTaskDto.getName());
        currentFilePipeline.setAiTaskTemplateId(aiTaskDto.getId());
    }

    private String translateOutputTargetType(OutputTargetType outputTargetType) {
        if (outputTargetType == null) {
            return "";
        }
        return translations.t("OutputTargetType." + outputTargetType);
    }

    private String translateInputSourceType(InputSourceType inputSourceType) {
        if (inputSourceType == null) {
            return "";
        }
        return translations.t("InputSourceType." + inputSourceType);
    }

    private HorizontalLayout createToolbar() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();

        Button addNewButton = new Button(translations.t("button.add"));
        addNewButton.addClickListener(ev -> openFormView(new FileProcessingPipelineDto()));

        addNewButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        horizontalLayout.add(addNewButton);
        return horizontalLayout;
    }

    private void openFormView(FileProcessingPipelineDto aCurrentFilePipeline) {
        currentFilePipeline = aCurrentFilePipeline;
        binder.readBean(currentFilePipeline);
        if (fileSystemInputSourceParamsView != null) {
            fileSystemInputSourceParamsView.readBean(aCurrentFilePipeline.getInputSource());
        }

        if (fileSystemOutputTargetParamsView != null) {
            fileSystemOutputTargetParamsView.readBean(aCurrentFilePipeline.getOutputTarget());
        }

        selectedTaskH4.setText(translations.t("fileProcessingPipeline.aiTaskToRun") + " " + aCurrentFilePipeline.getAiTaskTemplateName() != null ? aCurrentFilePipeline.getAiTaskTemplateName() : "");

        formLayout.setVisible(true);
        searchBarView.setVisible(false);
        filePipelineGrid.setVisible(false);
        paginationView.setVisible(false);
    }

    private void hideForm() {
        formLayout.setVisible(false);
        searchBarView.setVisible(true);
        filePipelineGrid.setVisible(true);
        paginationView.setVisible(true);
    }

    private Grid<FileProcessingPipelineDto> createGrid() {
        filePipelineGrid = new Grid<>(FileProcessingPipelineDto.class, false);
        filePipelineGrid.setSizeFull();
        filePipelineGrid.setEmptyStateText(translations.t("general.noItems"));
        filePipelineGrid.addSelectionListener(new SelectionListener<Grid<FileProcessingPipelineDto>, FileProcessingPipelineDto>() {
            @Override
            public void selectionChange(SelectionEvent<Grid<FileProcessingPipelineDto>, FileProcessingPipelineDto> selectionEvent) {
                if (selectionEvent.getFirstSelectedItem().isPresent()) {
                    paginationView.setCurrentPageInSession();
                }
            }
        });

        filePipelineGrid.addColumn(new ComponentRenderer<>(fileProcessingPipelineDto -> {
            VerticalLayout hl = new VerticalLayout();
            hl.setSizeFull();
            H4 h4 = new H4(fileProcessingPipelineDto.getName());
            h4.setSizeFull();
            h4.getStyle().set("text-overflow", "ellipsis");
            h4.getStyle().set("overflow-wrap", "break-word");
            h4.getStyle().set("white-space", "normal");

            hl.add(h4);
            Span countBadge = new Span(fileProcessingPipelineDto.getAiTaskTemplateName());
            countBadge.getElement().getThemeList().add("badge pill small contrast");

            String counterLabel = fileProcessingPipelineDto.getAiTaskTemplateName();
            countBadge.getElement().setAttribute("aria-label", counterLabel);
            countBadge.getElement().setAttribute("title", counterLabel);

            hl.add(countBadge);

            return hl;
        })).setFlexGrow(1);

        filePipelineGrid.addColumn(new ComponentRenderer<>(pipelineDto -> {
            HorizontalLayout actionsLayout = new HorizontalLayout();

            Icon deleteIcon = VaadinIcon.TRASH.create();
            deleteIcon.setColor("grey");

            deleteIcon.getElement().addEventListener("click", e -> {
                deleteAITaskConfirmationDialog(pipelineDto);
            }).addEventData("event.stopPropagation()");

            Icon editIcon = VaadinIcon.EDIT.create();
            editIcon.getElement().addEventListener("click", e -> {
                openFormView(pipelineDto);
            }).addEventData("event.stopPropagation()");

            Icon runIcon = VaadinIcon.PLAY.create();
            runIcon.getElement().addEventListener("click", e -> {
                runPipeline(pipelineDto);
            }).addEventData("event.stopPropagation()");

            actionsLayout.add(deleteIcon);
            actionsLayout.add(editIcon);

            actionsLayout.add(runIcon);
            actionsLayout.setJustifyContentMode(JustifyContentMode.END);
            return actionsLayout;
        }
        ));

        return filePipelineGrid;
    }

    private void runPipeline(FileProcessingPipelineDto pipelineDto) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(translations.t("fileProcessingPipeline.runPipelineTitle"));
        dialog.setText(
                translations.t("fileProcessingPipeline.runPipelineMessage", pipelineDto.getName()));

        dialog.setCancelable(true);
        dialog.setCancelText(translations.t("button.cancel"));

        dialog.setConfirmText(translations.t("button.run"));
        dialog.setConfirmButtonTheme("primary");
        dialog.addConfirmListener(event -> {
            int size = fileProcessingPipelineService.execute(pipelineDto.getId());

            if(size <= 1) {
                NotificationSupport.showInfo(translations.t("fileProcessingPipeline.taskPlannedSingular", size));
            } else {
                NotificationSupport.showInfo(translations.t("fileProcessingPipeline.tasksPlannedPlural", size));

            }
        });

        dialog.open();

    }

    private void deleteAITaskConfirmationDialog(FileProcessingPipelineDto fileProcessingPipelineDto) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(translations.t("fileProcessingPipeline.deleteFileProcessingPipelineTitle"));
        dialog.setText(
                translations.t("fileProcessingPipeline.deleteFileProcessingPipelineMessage", fileProcessingPipelineDto.getName()));

        dialog.setCancelable(true);
        dialog.setCancelText(translations.t("button.cancel"));

        dialog.setConfirmText(translations.t("button.delete"));
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            fileProcessingPipelineService.delete(fileProcessingPipelineDto);
            paginationView.loadCurrentPageAfterRemove();
            NotificationSupport.showSuccess(translations.t("notification.isDeleted", fileProcessingPipelineDto.getName()));
        });

        dialog.open();
    }

    private boolean saveAITask() {
        try {
            binder.writeBean(currentFilePipeline);
            if (fileSystemInputSourceParamsView != null) {
                fileSystemInputSourceParamsView.writeBean();
            }
            if (fileSystemOutputTargetParamsView != null) {
                fileSystemOutputTargetParamsView.writeBean();
            }

            if (currentFilePipeline.getAiTaskTemplateId() == null) {
                NotificationSupport.showError(translations.t("fileProcessingPipeline.aiTaskShouldBeSelected"));
                return false;
            }

            fileProcessingPipelineService.saveFileProcessingPipeline(currentFilePipeline);
            paginationView.loadCurrentPageAfterAddition();
            hideForm();

            NotificationSupport.showSuccess(translations.t("notification.isSaved", translations.t("fileProcessingPipeline.entityName")));
            return true;
        } catch (ValidationException ve) {
            ve.getValidationErrors().stream().forEach(v -> NotificationSupport.showError(v.getErrorMessage()));
            return false;
        }
    }

    @Override
    public String getPageTitle() {
        return translations.t("menu.fileProcessingPipeline");
    }
}
