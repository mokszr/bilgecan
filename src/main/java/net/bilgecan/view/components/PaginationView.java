package net.bilgecan.view.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.stream.IntStream;

public abstract class PaginationView extends HorizontalLayout {

    public static final int MAX_VISIBLE_PAGES = 3;
    private int pageSize = 10; // Adjust as needed
    private int currentPage = 0;
    private int totalPages = 0;
    private boolean doPrevOnRemoval = false;

    private final Button prevButton = new Button("<");
    private final Button nextButton = new Button(">");
    private final HorizontalLayout pageInfo = new HorizontalLayout();
    private String currentPageKey = "currentPage";
    private String searchTerm;

    public PaginationView() {
        prevButton.addClickListener(event -> {
            if (currentPage > 0) {
                currentPage--;
                loadPage(currentPage);
            }
        });

        nextButton.addClickListener(event -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadPage(currentPage);
            }
        });
        setAlignItems(Alignment.CENTER);
        setWidthFull();
        prevButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        nextButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        add(prevButton, pageInfo, nextButton);
    }

    public void loadCurrentPageAfterAddition() {
        loadPage(currentPage);
    }

    public void loadCurrentPageAfterRemove() {
        if (doPrevOnRemoval) {
            if (currentPage > 0) {
                currentPage--;
            }
        }
        doPrevOnRemoval = false;
        loadPage(currentPage);
    }

    public long loadPage(int page) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page termPage = loadAndGetPage(pageable, searchTerm);
        // Update total pages and control buttons
        totalPages = termPage.getTotalPages();
        doPrevOnRemoval = (currentPage) * pageSize == termPage.getTotalElements() - 1;
        currentPage = page;
        updatePaginationControls();
        return termPage.getTotalElements();
    }

    protected abstract Page loadAndGetPage(Pageable pageable, String searchTerm);

    private void updatePaginationControls() {
        // Clear existing buttons
        pageInfo.removeAll();

        // Update previous and next button states
        prevButton.setEnabled(currentPage > 0);
        nextButton.setEnabled(currentPage < totalPages - 1);

        // Calculate range of pages to display
        int startPage = Math.max(0, currentPage - 2);
        int endPage = Math.min(totalPages, startPage + MAX_VISIBLE_PAGES);

        if (endPage - startPage < MAX_VISIBLE_PAGES) {
            startPage = Math.max(0, endPage - MAX_VISIBLE_PAGES);
        }

        // Add numbered buttons for the visible pages
        IntStream.range(startPage, endPage).forEach(page -> {
            Button pageButton = new Button(String.valueOf(page + 1), e -> {
                currentPage = page;
                loadPage(currentPage);
            });
            pageButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
            pageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            if (page == currentPage) {
                pageButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
                pageButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            }
            pageInfo.add(pageButton);
        });

        // Add "..." if there are more pages after the visible range
        if (endPage < totalPages) {
            pageInfo.add(new Div(new Div("...")));
        }
    }

    // Set this variable whenever the page is changed
    public void setCurrentPageInSession() {
        // Optionally store it in the Vaadin session
        VaadinSession.getCurrent().setAttribute(currentPageKey, currentPage);
    }

    // Retrieve this variable when returning to the page
    public int getCurrentPageInSession() {
        Integer page = (Integer) VaadinSession.getCurrent().getAttribute(currentPageKey);
        return page != null ? page : 0;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setCurrentPageKey(String currentPageKey) {
        this.currentPageKey = currentPageKey;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }
}
