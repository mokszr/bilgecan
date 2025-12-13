package net.bilgecan.view;

import net.bilgecan.dto.AIResponseDetailsDto;
import net.bilgecan.dto.AITaskRunDto;
import net.bilgecan.entity.AITaskStatus;
import net.bilgecan.util.CustomUITools;
import net.bilgecan.service.AITaskRunService;
import net.bilgecan.service.TranslationService;
import net.bilgecan.support.NotificationSupport;
import net.bilgecan.view.components.PaginationView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Route(value = "aitaskruns", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN"})
public class AITaskRunView extends VerticalLayout implements HasDynamicTitle {

    private AITaskRunService aiTaskRunService;
    private final TranslationService translations;
    private Grid<AITaskRunDto> aiTaskRunGrid;
    private VerticalLayout detailsLayout;
    private PaginationView paginationView;
    private Markdown responseTextMarkDown;
    private H3 templateNameSpan;
    private HorizontalLayout inputFilePathLayout;
    private Span startFinishDetail;
    private DateTimeFormatter runDateTimeFormatter;
    private Span modelRouteDetail;
    private Span tokenDetail;
    private Span durationDetail;
    private Span finishDetail;

    public AITaskRunView(TranslationService translations, AITaskRunService aiTaskRunService, DateTimeFormatter runDateTimeFormatter) {
        this.translations = translations;
        this.aiTaskRunService = aiTaskRunService;
        this.runDateTimeFormatter = runDateTimeFormatter;
        setSizeFull();

        add(new H2(translations.t("aiTaskRun.runs")));

        HorizontalLayout toolbar = createToolbar();
        add(toolbar);

        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        this.aiTaskRunGrid = createGrid();
        paginationView = new PaginationView() {

            @Override
            protected Page loadAndGetPage(Pageable pageable, String searchTerm) {
                Page<AITaskRunDto> page = aiTaskRunService.findPaginated(pageable, searchTerm);
                // Update the grid with the current page data
                aiTaskRunGrid.setItems(page.getContent());
                return page;
            }
        };
        paginationView.setCurrentPageKey("currentPageAITaskRun");
        paginationView.loadPage(paginationView.getCurrentPageInSession());

        VerticalLayout left = new VerticalLayout(aiTaskRunGrid);
        left.setHeightFull();
        left.setMinWidth("400px");
        left.setMaxWidth("600px");
        left.setPadding(false);
        left.setMargin(false);

        left.add(paginationView);

        // Right: Details area
        this.detailsLayout = new VerticalLayout();
        detailsLayout.setSizeFull();

        this.templateNameSpan = new H3();
        this.responseTextMarkDown = new Markdown();
        this.inputFilePathLayout = new HorizontalLayout();
        this.inputFilePathLayout.setPadding(false);
        this.inputFilePathLayout.setVisible(false);
        VerticalLayout detailsContent = createDetailsContent();

        Details details = new Details(translations.t("aiTaskRun.executionDetails"), detailsContent);
        detailsLayout.setVisible(false);
        detailsLayout.add(templateNameSpan);
        detailsLayout.add(inputFilePathLayout);
        detailsLayout.add(responseTextMarkDown);
        detailsLayout.add(details);

        VerticalLayout right = new VerticalLayout(detailsLayout);
        right.setSizeFull();
        right.setPadding(true);

        splitLayout.addToPrimary(left);
        splitLayout.addToSecondary(right);
        splitLayout.setSplitterPosition(30);

        add(splitLayout);
    }


    private HorizontalLayout createToolbar() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();

        Button refreshButton = new Button(translations.t("button.refresh"));
        refreshButton.setIcon(VaadinIcon.REFRESH.create());

        refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshButton.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                paginationView.loadPage(paginationView.getCurrentPageInSession());
                NotificationSupport.showInfo(translations.t("general.refreshed"));
            }
        });


        horizontalLayout.add(refreshButton);
        return horizontalLayout;
    }

    private Grid<AITaskRunDto> createGrid() {
        Grid<AITaskRunDto> grid = new Grid<>(AITaskRunDto.class, false);
        grid.setSizeFull();
        grid.addSelectionListener(new SelectionListener<Grid<AITaskRunDto>, AITaskRunDto>() {
            @Override
            public void selectionChange(SelectionEvent<Grid<AITaskRunDto>, AITaskRunDto> selectionEvent) {
                if (selectionEvent.getFirstSelectedItem().isPresent()) {
                    AITaskRunDto dto = selectionEvent.getFirstSelectedItem().get();
                    updateDetails(dto);
                }
            }
        });
        grid.addColumn(new ComponentRenderer<com.vaadin.flow.component.Component, AITaskRunDto>(aiTaskRun -> {
            VerticalLayout vl = new VerticalLayout();
            vl.setSizeFull();
            H4 h4 = new H4("#" + aiTaskRun.getId().toString() + " " + aiTaskRun.getTemplateName());
            h4.setSizeFull();
            h4.getStyle().set("text-overflow", "ellipsis");
            h4.getStyle().set("overflow-wrap", "break-word");
            h4.getStyle().set("white-space", "normal");

            vl.add(h4);

            Span statusBadge = new Span(aiTaskRun.getStatus().name());
            statusBadge.getElement().getThemeList().add("badge primary pill " + CustomUITools.getStatusBadge(aiTaskRun.getStatus()));

            vl.add(statusBadge);

            Span started = new Span("Start: " + formatTime(aiTaskRun.getStartedAt()));
            started.getElement().getThemeList().add("badge contrast small pill ");
            Span finished = new Span("Finish: " + formatTime(aiTaskRun.getFinishedAt()));
            finished.getElement().getThemeList().add("badge contrast small pill ");

            Icon deleteIcon = VaadinIcon.TRASH.create();
            deleteIcon.setColor("gray");
            Button deleteButton = new Button(deleteIcon);
            deleteButton.addClickListener(e -> {
                deleteAITaskRunConfirmationDialog(aiTaskRun);
            });
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            deleteButton.setTooltipText(translations.t("button.delete"));

            final AITaskStatus status = aiTaskRun.getStatus();
            if (status.equals(AITaskStatus.RUNNING)) {
                deleteButton.setVisible(false);
            }

            Icon cancelIcon = VaadinIcon.CLOSE_CIRCLE_O.create();
            cancelIcon.setColor("gray");
            Button cancelButton = new Button(cancelIcon);
            cancelButton.setTooltipText(translations.t("button.cancel"));
            cancelButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
                cancelAITaskRunConfirmationDialog(aiTaskRun);
            });
            cancelButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);

            if (status.equals(AITaskStatus.DONE) ||
                    status.equals(AITaskStatus.FAILED) ||
                    status.equals(AITaskStatus.CANCELED)) {
                cancelButton.setVisible(false);
            }

            HorizontalLayout timesLayout = new HorizontalLayout(started, finished);

            vl.add(timesLayout);

            HorizontalLayout buttonLayout = new HorizontalLayout(deleteButton, cancelButton);

            vl.add(buttonLayout);

            return vl;
        })).setFlexGrow(1);

        return grid;
    }

    private void cancelAITaskRunConfirmationDialog(AITaskRunDto aiTask) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(translations.t("aiTaskRun.cancelAITaskRunTitle"));
        dialog.setText(
                translations.t("aiTaskRun.cancelAITaskRunMessage", "#" + aiTask.getId() + " " + aiTask.getTemplateName()));

        dialog.setCancelable(true);
        dialog.setCancelText(translations.t("button.cancel"));

        dialog.setConfirmText(translations.t("button.proceed"));
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            boolean canceled = aiTaskRunService.cancel(aiTask);
            paginationView.loadCurrentPageAfterAddition();
            if (canceled) {
                NotificationSupport.showSuccess(translations.t("notification.isCanceled", "#" + aiTask.getId() + " " + aiTask.getTemplateName()));
            } else {
                NotificationSupport.showInfo(translations.t("notification.cannotBeCanceled", "#" + aiTask.getId() + " " + aiTask.getTemplateName()));
            }
        });

        dialog.open();
    }

    private void deleteAITaskRunConfirmationDialog(AITaskRunDto aiTask) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(translations.t("aiTaskRun.deleteAITaskRunTitle"));
        dialog.setText(
                translations.t("aiTaskRun.deleteAITaskRunMessage", "#" + aiTask.getId() + " " + aiTask.getTemplateName()));

        dialog.setCancelable(true);
        dialog.setCancelText(translations.t("button.cancel"));

        dialog.setConfirmText(translations.t("button.delete"));
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            aiTaskRunService.delete(aiTask);
            paginationView.loadCurrentPageAfterRemove();
            NotificationSupport.showSuccess(translations.t("notification.isDeleted", "#" + aiTask.getId() + " " + aiTask.getTemplateName()));
            updateDetails(new AITaskRunDto());
        });

        dialog.open();
    }

    private String formatTime(OffsetDateTime time) {
        //TODO take Zone info from user profile settings
        return time != null ? runDateTimeFormatter.withZone(ZoneId.systemDefault()).format(time) : "-";
    }

    private void updateDetails(AITaskRunDto dto) {
        detailsLayout.setVisible(true);
        inputFilePathLayout.removeAll();

        if (dto.getAiResponseDetails() == null) {
            if (AITaskStatus.FAILED.equals(dto.getStatus())) {
                responseTextMarkDown.setContent(translations.t("aiTaskRun.failedTitle") + " " + dto.getError());
            } else if (AITaskStatus.CANCELED.equals(dto.getStatus())) {
                responseTextMarkDown.setContent(translations.t("aiTaskRun.canceledTitle"));
            } else if (dto.getId() == null) {
                responseTextMarkDown.setContent("");
            } else {
                responseTextMarkDown.setContent(translations.t("aiTaskRun.stillRunning"));
            }
            startFinishDetail.setText("");
            modelRouteDetail.setText("Model:");
            tokenDetail.setText("Tokens: prompt:");
            durationDetail.setText("Durations(ms):");
            finishDetail.setText("Finish reason: ");
        } else {
            responseTextMarkDown.setContent(dto.getAiResponseDetails().getOutputTextAi());
            AIResponseDetailsDto aiResponseDetails = dto.getAiResponseDetails();
            modelRouteDetail.setText("Model: " + dto.getModelRoute());
            tokenDetail.setText("Tokens: prompt: " + aiResponseDetails.getPromptTokensAi() + " completion:" + aiResponseDetails.getCompletionTokensAi() + " Total: " + aiResponseDetails.getTotalTokensAi());
            durationDetail.setText("Durations(ms): load: " + aiResponseDetails.getLoadDurationAi() + " eval: " + aiResponseDetails.getEvalDurationAi() + " Total: " + aiResponseDetails.getTotalDurationAi());
            finishDetail.setText("Finish reason: " + aiResponseDetails.getFinishReasonAi() + " Done: " + aiResponseDetails.getDoneAi());
            startFinishDetail.setText("Start: " + formatTime(dto.getStartedAt()) + " Finish: " + formatTime(dto.getFinishedAt()) + " status: " + dto.getStatus());

        }
        if (dto.getId() == null) {
            templateNameSpan.setText("");
            detailsLayout.setVisible(false);
        } else {
            templateNameSpan.setText("#" + dto.getId() + " " + dto.getTemplateName());
            if (dto.getInputFilePath() != null) {
                H4 h4 = new H4();

                h4.setText(translations.t("aiTaskRun.inputFilePath", dto.getInputFilePath(), dto.getInputFileMimeType()));
                inputFilePathLayout.add(h4);
                Button showImageButton = new Button(translations.t("button.showImage"));
                showImageButton.addClickListener(buttonClickEvent -> {
                    openImageDialog(dto);
                });
                inputFilePathLayout.add(showImageButton);
                inputFilePathLayout.setVisible(true);
            } else {
                inputFilePathLayout.setVisible(false);

            }
        }
    }

    private VerticalLayout createDetailsContent() {
        modelRouteDetail = new Span();
        modelRouteDetail.getElement().getThemeList().add("badge contrast small pill ");
        tokenDetail = new Span();
        tokenDetail.getElement().getThemeList().add("badge contrast small pill ");
        durationDetail = new Span();
        durationDetail.getElement().getThemeList().add("badge contrast small pill ");
        finishDetail = new Span();
        finishDetail.getElement().getThemeList().add("badge contrast small pill ");
        startFinishDetail = new Span();
        startFinishDetail.getElement().getThemeList().add("badge contrast small pill ");

        VerticalLayout detailsContent = new VerticalLayout(startFinishDetail, tokenDetail, durationDetail, modelRouteDetail, finishDetail);
        detailsContent.setSpacing(false);
        detailsContent.setPadding(false);
        return detailsContent;
    }

    @Override
    public String getPageTitle() {
        return translations.t("menu.runs");
    }

    private void openImageDialog(AITaskRunDto dto) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(dto.getInputFilePath());
        dialog.setModal(true);
        dialog.setResizable(true);
        dialog.setDraggable(true);

        Optional<File> fullInputFile = aiTaskRunService.getFullInputFile(dto);
        if (fullInputFile.isEmpty()) {
            NotificationSupport.showError(translations.t("aiTaskRun.inputImageFileNotFound"));
            return;
        }

        Image img = new Image(DownloadHandler.forFile(fullInputFile.get()), translations.t("aiTaskRun.imagePreview"));

        img.setMaxWidth("80vw");
        img.setMaxHeight("80vh");
        img.getStyle().set("object-fit", "contain");

        dialog.add(img);
        Button close = new Button(translations.t("button.close"), e -> dialog.close());
        HorizontalLayout footer = new HorizontalLayout(close);
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.CENTER);
        footer.setSpacing(true);

        dialog.getFooter().add(footer);
        dialog.open();
    }

}
