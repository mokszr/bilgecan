package net.bilgecan.view;

import net.bilgecan.dto.FeedRagHistoryDto;
import net.bilgecan.service.FeedRAGService;
import net.bilgecan.service.TranslationService;
import net.bilgecan.support.NotificationSupport;
import net.bilgecan.view.components.PaginationView;
import net.bilgecan.view.components.UploadI18NSupport;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.FileUploadCallback;
import com.vaadin.flow.server.streams.TemporaryFileUploadHandler;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.server.streams.UploadMetadata;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Route(value = "feedrag", layout = MainLayout.class)
@RolesAllowed({"ADMIN"})
public class FeedRAGView extends VerticalLayout implements HasDynamicTitle {

    private static final Logger log = LoggerFactory.getLogger(FeedRAGView.class);
    private final String FEED_METHOD_USERTEXT = "USERTEXT";
    private final String FEED_METHOD_FILE = "FILE";

    private final Grid<FeedRagHistoryDto> feedHistoryGrid;
    private final TextField userTextNameField;
    private final VerticalLayout userTextLayout;
    private final Upload upload;
    private final TextArea userTextArea;
    private final H4 acceptedFileFormatsTitle;
    private final VerticalLayout uploadLayout;
    private TranslationService translations;
    private FeedRAGService feedRAGService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private PaginationView paginationView;

    public FeedRAGView(FeedRAGService feedRAGService, TranslationService translations) {
        this.translations = translations;
        this.feedRAGService = feedRAGService;
        setSizeFull();
        add(new H2(translations.t("feedRag.title")));

        ComboBox<String> feedMethodComboBox = new ComboBox<>(translations.t("feedRag.feedMethod"));
        feedMethodComboBox.setItems(Arrays.asList(FEED_METHOD_FILE, FEED_METHOD_USERTEXT));
        feedMethodComboBox.setItemLabelGenerator(s -> translations.t("feedRag.feedMethod." + s));
        feedMethodComboBox.setValue(FEED_METHOD_FILE);

        add(feedMethodComboBox);

        userTextLayout = new VerticalLayout();
        userTextLayout.setPadding(false);
        userTextLayout.setWidthFull();
        userTextLayout.setVisible(false);

        userTextNameField = new TextField(translations.t("feedRag.userTextNameLabel"));
        userTextNameField.setPlaceholder(translations.t("feedRag.userTextNamePlaceholder"));
        userTextNameField.setWidthFull();

        userTextArea = new TextArea(translations.t("feedRag.userTextArea"));
        userTextArea.setPlaceholder(translations.t("feedRag.userTextAreaPlaceholder"));
        userTextArea.setWidthFull();
        userTextArea.setMaxRows(10);
        userTextArea.setMinHeight("250px");

        Button submitTextButton = new Button(translations.t("button.submit"));
        submitTextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitTextButton.addClickListener(buttonClickEvent -> {
            String nameValue = userTextNameField.getValue();
            if (StringUtils.isBlank(nameValue)) {
                userTextNameField.setInvalid(true);
            }
            String textValue = userTextArea.getValue();
            if (StringUtils.isBlank(textValue)) {
                userTextArea.setInvalid(true);
            }

            if (userTextArea.isInvalid() || userTextNameField.isInvalid()) {
                NotificationSupport.showError(translations.t("feedRag.invalidUserText"));
            } else {
                feedRAGService.feedPlainText(textValue, nameValue);
                NotificationSupport.showSuccess(translations.t("feedRag.feedSuccess") + " " + nameValue);
                userTextNameField.setValue("");
                userTextArea.setValue("");
                paginationView.loadCurrentPageAfterAddition();
            }

        });

        userTextLayout.add(userTextNameField);
        userTextLayout.add(userTextArea);
        userTextLayout.add(submitTextButton);

        add(userTextLayout);

        uploadLayout = new VerticalLayout();
        uploadLayout.setPadding(false);
        uploadLayout.setHeight("200px");

        acceptedFileFormatsTitle = new H4(translations.t("feedRag.acceptedFileFormats"));
        uploadLayout.add(acceptedFileFormatsTitle);

        TemporaryFileUploadHandler uploadHandler = UploadHandler.toTempFile(new FileUploadCallback() {
            @Override
            public void complete(UploadMetadata uploadMetadata, File file) throws IOException {

                FeedRAGService.FeedResult feedResult = feedRAGService.feedFile(file, uploadMetadata.fileName());

                if (!feedResult.success()) {
                    NotificationSupport.showError(translations.t("feedRag.feedFailed") + " " + feedResult.error());
                } else {
                    NotificationSupport.showSuccess(translations.t("feedRag.feedSuccess") + " " + feedResult.name());
                    paginationView.loadCurrentPageAfterAddition();
                }

            }
        });

        upload = new Upload(uploadHandler);
        upload.setAcceptedFileTypes("application/pdf",
                "text/plain",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "text/html",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        upload.setWidthFull();
        upload.setMinHeight("150px");
        int maxFileSizeInBytes = 100 * 1024 * 1024; // 100MB
        upload.setMaxFileSize(maxFileSizeInBytes);
        upload.setMaxFiles(10);
        upload.addFileRejectedListener(event -> {
            String errorMessage = event.getErrorMessage();

            NotificationSupport.showError(errorMessage);

        });

        upload.setI18n(new UploadI18NSupport(translations));
        upload.getStyle().setOverflow(Style.Overflow.AUTO);
        uploadLayout.add(upload);

        feedHistoryGrid = createFeedHistoryGrid();
        paginationView = new PaginationView() {

            @Override
            protected Page loadAndGetPage(Pageable pageable, String searchTerm) {
                Page<FeedRagHistoryDto> page = feedRAGService.findPaginated(pageable);
                // Update the grid with the current page data
                feedHistoryGrid.setItems(page.getContent());
                return page;
            }
        };
        paginationView.setCurrentPageKey("currentPageFeedRagHistory");
        paginationView.loadPage(paginationView.getCurrentPageInSession());

        feedMethodComboBox.addValueChangeListener(event -> {
            String selectedMethod = event.getValue();
            if (selectedMethod.equals(FEED_METHOD_FILE)) {
                uploadLayout.setVisible(true);
                userTextLayout.setVisible(false);
            } else if (selectedMethod.equals(FEED_METHOD_USERTEXT)) {
                uploadLayout.setVisible(false);
                userTextLayout.setVisible(true);
            }
        });


        add(uploadLayout);
        add(new H4(translations.t("feedRag.feedRAGHistory")));
        add(feedHistoryGrid);
        add(paginationView);
    }

    private Grid<FeedRagHistoryDto> createFeedHistoryGrid() {
        Grid<FeedRagHistoryDto> grid = new Grid<>(FeedRagHistoryDto.class, false);
        grid.setSizeFull();
        grid.setEmptyStateText(translations.t("general.noItems"));

        grid.addColumn(FeedRagHistoryDto::getFileName)
                .setHeader(translations.t("feedRag.column.fileName"))
                .setTooltipGenerator(FeedRagHistoryDto::getFileName)
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(FeedRagHistoryDto::getMimeType)
                .setHeader(translations.t("feedRag.column.mimeType"))
                .setTooltipGenerator(FeedRagHistoryDto::getMimeType)
                .setAutoWidth(true);

        grid.addColumn(dto -> dto.getDate() != null ? formatter.withZone(ZoneId.systemDefault()).format(dto.getDate()) : "-")
                .setHeader(translations.t("feedRag.column.date"))
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(FeedRagHistoryDto::getSize)
                .setHeader(translations.t("feedRag.column.size"))
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.END);

        // Delete button column
        grid.addComponentColumn(dto -> {
                    Button deleteBtn = new Button(translations.t("button.delete"), e -> {
                        deleteFeedRagHistoryConfirmationDialog(dto);
                    });
                    deleteBtn.getElement().getThemeList().add("error primary small");
                    return deleteBtn;
                }).setHeader(translations.t("feedRag.column.actions"))
                .setAutoWidth(true)
                .setFlexGrow(0);

        return grid;
    }

    private void deleteFeedRagHistoryConfirmationDialog(FeedRagHistoryDto dto) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(translations.t("feedRag.deleteFeedRagHistoryTitle"));
        dialog.setText(
                translations.t("feedRag.deleteFeedRagHistoryMessage", dto.getFileName()));

        dialog.setCancelable(true);
        dialog.setCancelText(translations.t("button.cancel"));

        dialog.setConfirmText(translations.t("button.delete"));
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            feedRAGService.delete(dto);
            paginationView.loadCurrentPageAfterRemove();
            NotificationSupport.showSuccess(translations.t("notification.isDeleted", dto.getFileName()));
        });

        dialog.open();
    }

    @Override
    public String getPageTitle() {
        return translations.t("menu.feedRAG");
    }
}
