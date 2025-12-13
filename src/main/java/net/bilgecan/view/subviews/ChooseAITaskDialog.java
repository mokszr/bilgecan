package net.bilgecan.view.subviews;

import net.bilgecan.dto.AITaskDto;
import net.bilgecan.service.AITaskTemplateService;
import net.bilgecan.service.TranslationService;
import net.bilgecan.support.NotificationSupport;
import net.bilgecan.view.components.PaginationView;
import net.bilgecan.view.components.SearchBarView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.function.Consumer;

public class ChooseAITaskDialog extends Dialog {

    private AITaskTemplateService aiTaskTemplateService;
    private Consumer<AITaskDto> selectionHandler;
    private TranslationService translations;
    private final SearchBarView searchBarView;
    private PaginationView paginationView;
    private Grid<AITaskDto> aiTaskGrid;

    public ChooseAITaskDialog(AITaskTemplateService aiTaskTemplateService, Consumer<AITaskDto> selectionHandler, TranslationService translations) {
        this.aiTaskTemplateService = aiTaskTemplateService;
        this.selectionHandler = selectionHandler;
        this.translations = translations;
        setModal(true);
        setDraggable(true);
        setResizable(false);
        setWidth("90%");
        setMaxWidth("1000px");
        setMinWidth("300px");
        setMinHeight("600px");
        setHeaderTitle(translations.t("chooseAITaskDialog.title"));

        searchBarView = new SearchBarView(translations) {

            @Override
            public void searchClicked(String searchValue) {
                paginationView.setSearchTerm(searchValue);
                long foundCount = paginationView.loadPage(0);
                NotificationSupport.showInfo(translations.t("general.search.foundNotification", foundCount));
            }
        };

        searchBarView.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        Grid<AITaskDto> grid = createGrid();

        paginationView = new PaginationView() {

            @Override
            protected Page loadAndGetPage(Pageable pageable, String searchTerm) {
                Page<AITaskDto> termPage = aiTaskTemplateService.findPaginated(pageable, searchTerm);
                // Update the grid with the current page data
                aiTaskGrid.setItems(termPage.getContent());
                return termPage;
            }
        };
        paginationView.setCurrentPageKey("currentPageChooseAITask");
        paginationView.setPageSize(4);
        paginationView.loadPage(paginationView.getCurrentPageInSession());

        Button cancelButton = new Button(translations.t("button.cancel"));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(ev -> {
            close();
        });

        HorizontalLayout buttons = new HorizontalLayout(cancelButton);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.setWidthFull();

        VerticalLayout dialogLayout = new VerticalLayout(searchBarView, grid, paginationView);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);
        dialogLayout.setWidthFull();
        dialogLayout.setMaxHeight("500px");

        add(dialogLayout, buttons);
    }

    private Grid<AITaskDto> createGrid() {
        aiTaskGrid = new Grid<>(AITaskDto.class, false);
        aiTaskGrid.setWidthFull();
        aiTaskGrid.setMaxHeight("500px");

        aiTaskGrid.addColumn(new ComponentRenderer<>(aiTask -> {
            VerticalLayout hl = new VerticalLayout();
            hl.setSizeFull();
            H4 h4 = new H4(aiTask.getName());
            h4.setSizeFull();
            h4.getStyle().set("text-overflow", "ellipsis");
            h4.getStyle().set("overflow-wrap", "break-word");
            h4.getStyle().set("white-space", "normal");

            hl.add(h4);
            //Span countBadge = new Span(Integer.toString(aiTask.getCount()));
            Span countBadge = new Span(aiTask.getPrompt());
            countBadge.getElement().getThemeList().add("badge pill small contrast");

            //String counterLabel = translations.t("aiTask.termsCount", aiTask.getCount());
            String counterLabel = translations.t("aiTaskTemplate.userPrompt", aiTask.getPrompt());
            countBadge.getElement().setAttribute("aria-label", counterLabel);
            countBadge.getElement().setAttribute("title", counterLabel);

            hl.add(countBadge);

            return hl;
        })).setFlexGrow(4);

        aiTaskGrid.addColumn(new ComponentRenderer<>(aiTaskDto -> {

            Icon arrowRightIcon = VaadinIcon.ARROW_RIGHT.create();
            arrowRightIcon.setColor("grey");

            Button chooseButton = new Button(translations.t("button.choose"), arrowRightIcon);
            chooseButton.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
                @Override
                public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                    taskSelected(aiTaskDto);
                }
            });

            return chooseButton;
        }
        )).setFlexGrow(1);

        return aiTaskGrid;
    }

    private void taskSelected(AITaskDto aiTaskDto) {
        selectionHandler.accept(aiTaskDto);
        this.close();
    }

}
