package net.bilgecan.entity;

import net.bilgecan.entity.security.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ai_task_run")
public class AITaskRun {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ai_task_run_seq")
    @SequenceGenerator(name = "ai_task_run_seq", sequenceName = "ai_task_run_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false)
    private String templateName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private AITaskTemplate template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    // Main user-facing prompt to run
    @Column(nullable = false, columnDefinition = "text")
    private String rawPrompt;

    //parameters resolved prompt
    @Column(nullable = false, columnDefinition = "text")
    private String resolvedPrompt;

    @Column(columnDefinition = "text")
    private String rawSystemPrompt;

    @Column(columnDefinition = "text")
    private String resolvedSystemPrompt;

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private AITaskStatus status;

    @Column(nullable = false)
    private String modelRoute;

    @Column(columnDefinition = "text")
    private String jsonSchema;

    @Column(length = 1000)
    private String inputFilePath;

    @Column
    private String inputFileMimeType;

    @Column
    private String inputFileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_pipeline_id")
    private FileProcessingPipeline fileProcessingPipeline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    /** Effective priority for this run (copied from template or overridden) */
    @Column(nullable = false)
    private Integer priority = 100;

    @Column
    private String leaseOwner;

    @Column
    private OffsetDateTime leaseUntil;

    @Column(columnDefinition = "text")
    private String error;

    @Enumerated
    private AIResponseDetails aiResponseDetails;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column
    private OffsetDateTime startedAt;

    @Column
    private OffsetDateTime finishedAt;

    @Version
    private Long version;    // optimistic lock version (JPA)

    @Column(nullable = false)
    private boolean enableRag = Boolean.FALSE;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AITaskTemplate getTemplate() {
        return template;
    }

    public void setTemplate(AITaskTemplate template) {
        this.template = template;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getRawPrompt() {
        return rawPrompt;
    }

    public void setRawPrompt(String rawPrompt) {
        this.rawPrompt = rawPrompt;
    }

    public String getResolvedPrompt() {
        return resolvedPrompt;
    }

    public void setResolvedPrompt(String resolvedPrompt) {
        this.resolvedPrompt = resolvedPrompt;
    }

    public String getRawSystemPrompt() {
        return rawSystemPrompt;
    }

    public void setRawSystemPrompt(String rawSystemPrompt) {
        this.rawSystemPrompt = rawSystemPrompt;
    }

    public String getResolvedSystemPrompt() {
        return resolvedSystemPrompt;
    }

    public void setResolvedSystemPrompt(String resolvedSystemPrompt) {
        this.resolvedSystemPrompt = resolvedSystemPrompt;
    }

    public AITaskStatus getStatus() {
        return status;
    }

    public void setStatus(AITaskStatus status) {
        this.status = status;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getLeaseOwner() {
        return leaseOwner;
    }

    public void setLeaseOwner(String leaseOwner) {
        this.leaseOwner = leaseOwner;
    }

    public OffsetDateTime getLeaseUntil() {
        return leaseUntil;
    }

    public void setLeaseUntil(OffsetDateTime leaseUntil) {
        this.leaseUntil = leaseUntil;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public OffsetDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(OffsetDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getModelRoute() {
        return modelRoute;
    }

    public void setModelRoute(String modelRoute) {
        this.modelRoute = modelRoute;
    }

    public AIResponseDetails getAiResponseDetails() {
        return aiResponseDetails;
    }

    public void setAiResponseDetails(AIResponseDetails aiResponseDetails) {
        this.aiResponseDetails = aiResponseDetails;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getJsonSchema() {
        return jsonSchema;
    }

    public void setJsonSchema(String jsonSchema) {
        this.jsonSchema = jsonSchema;
    }

    public String getInputFilePath() {
        return inputFilePath;
    }

    public void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    public String getInputFileMimeType() {
        return inputFileMimeType;
    }

    public void setInputFileMimeType(String inputFileMimeType) {
        this.inputFileMimeType = inputFileMimeType;
    }

    public FileProcessingPipeline getFileProcessingPipeline() {
        return fileProcessingPipeline;
    }

    public void setFileProcessingPipeline(FileProcessingPipeline fileProcessingPipeline) {
        this.fileProcessingPipeline = fileProcessingPipeline;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public String getInputFileName() {
        return inputFileName;
    }

    public void setInputFileName(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    public boolean isEnableRag() {
        return enableRag;
    }

    public void setEnableRag(boolean enableRag) {
        this.enableRag = enableRag;
    }
}
