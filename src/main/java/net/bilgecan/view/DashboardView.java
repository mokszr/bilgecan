package net.bilgecan.view;

import net.bilgecan.init.OllamaModelRegistry;
import net.bilgecan.service.AITaskExecutorService;
import net.bilgecan.service.SystemMetricsService;
import net.bilgecan.service.TranslationService;
import net.bilgecan.support.NotificationSupport;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.Arrays;
import java.util.List;

@Route(value = "dashboard", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN"})
public class DashboardView extends VerticalLayout implements HasDynamicTitle {

    private AITaskExecutorService executorService;
    private SystemMetricsService systemMetricsService;
    private TranslationService translations;
    private OllamaModelRegistry ollamaModelRegistry;

    private final Span jvmUsed = new Span();
    private final Span jvmMax = new Span();
    private final Span sysTotal = new Span();
    private final Span sysFree = new Span();

    private final Span procCpu = new Span();
    private final Span sysCpu = new Span();
    private final Span procs = new Span();

    private final Span activeCount = new Span();
    private final Span remainingCapacity = new Span();

    private ListBox<String> ollamaModelListBox;


    public DashboardView(AITaskExecutorService executorService, SystemMetricsService systemMetricsService, OllamaModelRegistry ollamaModelRegistry, TranslationService translations) {
        this.translations = translations;
        this.executorService = executorService;
        this.systemMetricsService = systemMetricsService;
        this.ollamaModelRegistry = ollamaModelRegistry;

        add(new H2(translations.t("dashboard.title")));

        add(new H4(translations.t("dashboard.workerId") + " " + executorService.getWorkerId()));

        HorizontalLayout row1 = new HorizontalLayout();
        row1.setWidthFull();
        row1.setAlignItems(FlexComponent.Alignment.STRETCH); // cards will stretch to same height
        row1.setJustifyContentMode(JustifyContentMode.START);

        add(row1);

        row1.add(createSystemMetricsCard());
        row1.add(createLlmModelsCard());

        HorizontalLayout row2 = new HorizontalLayout();
        row2.setWidthFull();
        row2.setAlignItems(FlexComponent.Alignment.STRETCH);
        row2.setJustifyContentMode(JustifyContentMode.START);

        add(row2);

        row2.add(createExecutorThreadPoolCard());

    }

    private Card createExecutorThreadPoolCard() {
        Card poolDetailsCard = new Card();
        poolDetailsCard.setTitle(translations.t("dashboard.poolDetails.title"));
        Button refreshButton = new Button(translations.t("button.refresh"));
        refreshButton.setIcon(VaadinIcon.REFRESH.create());
        refreshButton.addClickListener(buttonClickEvent -> refreshPoolDetails());

        poolDetailsCard.setHeaderSuffix(refreshButton);

        poolDetailsCard.add(createInfoRow(translations.t("dashboard.poolDetails.activeCount"), activeCount));
        poolDetailsCard.add(createInfoRow(translations.t("dashboard.poolDetails.remainingCapacity"), remainingCapacity));

        updatePoolDetails();

        return poolDetailsCard;
    }

    private void refreshPoolDetails() {
        updatePoolDetails();
        NotificationSupport.showInfo(translations.t("general.refreshed"));
    }

    private void updatePoolDetails() {
        activeCount.setText(Integer.toString(executorService.getActiveCount()));
        remainingCapacity.setText(Integer.toString(executorService.getRemainingCapacity()));
    }

    private Card createLlmModelsCard() {
        Card llmModelsCard = new Card();
        llmModelsCard.setTitle(translations.t("dashboard.llmModels.title"));
        Button refreshButton = new Button(translations.t("button.refresh"));
        refreshButton.setIcon(VaadinIcon.REFRESH.create());
        refreshButton.addClickListener(buttonClickEvent -> refreshLLMs());


        llmModelsCard.setHeaderSuffix(refreshButton);

        ollamaModelListBox = new ListBox<>();
        ollamaModelListBox.setHeight("250px");
        updateOllamaModelsListBox();
        ollamaModelListBox.setReadOnly(true);

        llmModelsCard.add(ollamaModelListBox);

        return llmModelsCard;
    }

    private void refreshLLMs() {
        ollamaModelRegistry.refresh();
        updateOllamaModelsListBox();
        NotificationSupport.showInfo(translations.t("general.refreshed"));
    }

    private void updateOllamaModelsListBox() {
        List<String> models = ollamaModelRegistry.getModels();

        if (models.isEmpty()) {
            ollamaModelListBox.setItems(Arrays.asList(translations.t("dashboard.llmModels.empty")));
        } else {
            ollamaModelListBox.setItems(models);
        }
    }

    private Card createSystemMetricsCard() {
        Card systemMetricsCard = new Card();
        systemMetricsCard.setTitle(translations.t("dashboard.systemMetrics.title"));
        Button refreshButton = new Button(translations.t("button.refresh"));
        refreshButton.setIcon(VaadinIcon.REFRESH.create());
        refreshButton.addClickListener(buttonClickEvent -> refreshMetrics());

        systemMetricsCard.setHeaderSuffix(refreshButton);

        systemMetricsCard.add(createInfoRow("JVM used (MB): ", jvmUsed));
        systemMetricsCard.add(createInfoRow("JVM max (MB): ", jvmMax));
        systemMetricsCard.add(createInfoRow("System total RAM (MB): ", sysTotal));
        systemMetricsCard.add(createInfoRow("System free RAM (MB): ", sysFree));
        systemMetricsCard.add(createInfoRow("Process CPU (%): ", procCpu));
        systemMetricsCard.add(createInfoRow("System CPU (%): ", sysCpu));
        systemMetricsCard.add(createInfoRow("Processors: ", procs));

        updateMetrics();
        return systemMetricsCard;
    }

    private void refreshMetrics() {
        updateMetrics();
        NotificationSupport.showInfo(translations.t("general.refreshed"));
    }

    private void updateMetrics() {
        // read metrics (safe from any thread)
        long jvmUsedMb = systemMetricsService.getJvmUsedMemoryMb();
        long jvmMaxMb = systemMetricsService.getJvmMaxMemoryMb();
        long sysTotalMb = systemMetricsService.getSystemTotalMemoryMb();
        long sysFreeMb = systemMetricsService.getSystemFreeMemoryMb();
        double procCpuPct = systemMetricsService.getProcessCpuLoadPercent();
        double sysCpuPct = systemMetricsService.getSystemCpuLoadPercent();
        int cores = systemMetricsService.getAvailableProcessors();

        jvmUsed.setText(String.valueOf(jvmUsedMb));
        jvmMax.setText(String.valueOf(jvmMaxMb));
        sysTotal.setText(sysTotalMb < 0 ? "N/A" : String.valueOf(sysTotalMb));
        sysFree.setText(sysFreeMb < 0 ? "N/A" : String.valueOf(sysFreeMb));
        procCpu.setText(procCpuPct < 0 ? "N/A" : procCpuPct + " %");
        sysCpu.setText(sysCpuPct < 0 ? "N/A" : sysCpuPct + " %");
        procs.setText(String.valueOf(cores));
    }


    private Component createInfoRow(String label, Span value) {
        Div row = new Div();
        row.getStyle().set("display", "flex");
        row.getStyle().set("gap", "8px");
        Span lbl = new Span(label);
        lbl.getStyle().set("width", "220px");
        value.getStyle().set("font-weight", "600");
        row.add(lbl, value);
        return row;
    }

    @Override
    public String getPageTitle() {
        return translations.t("menu.dashboard");
    }
}
