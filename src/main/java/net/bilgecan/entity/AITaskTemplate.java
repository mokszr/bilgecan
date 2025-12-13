package net.bilgecan.entity;

import net.bilgecan.entity.security.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ai_task_template")
public class AITaskTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Main user-facing prompt to run
    @Column(nullable = false, columnDefinition = "text")
    private String prompt;

    // Optional system prompt (must be PromptType.SYSTEM; enforce in service)
    @Column(columnDefinition = "text")
    private String systemPrompt;

    // Output placement into execution context
    @Column(name = "output_var_name")
    private String outputVarName; // e.g. "result", "summary"

    // Model route hint (e.g., "ollama:llama3:8b" or "openai:gpt-4o")
    @Column(nullable = false)
    private String modelRoute;

    /** User-specified default priority for runs (lower = higher priority) */
    @Column(nullable = false)
    private Integer defaultPriority = 100;

    // optional jsonSchema to get a structured json response
    @Column(columnDefinition = "text")
    private String jsonSchema;

    @Column(length = 1000)
    private String inputFilePath;

    @Column
    private String inputFileMimeType;

    // Variable bindings for the template variables of the selected Prompt
    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<AITaskVarBinding> varBindings = new ArrayList<>();

    @Column(nullable = false)
    private java.time.OffsetDateTime createdAt = java.time.OffsetDateTime.now();

    @Column(nullable = false)
    private java.time.OffsetDateTime updatedAt = java.time.OffsetDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @Column(nullable = false)
    private boolean enableRag = Boolean.FALSE;

    // getters/setters...


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOutputVarName() {
        return outputVarName;
    }

    public void setOutputVarName(String outputVarName) {
        this.outputVarName = outputVarName;
    }

    public String getModelRoute() {
        return modelRoute;
    }

    public void setModelRoute(String modelRoute) {
        this.modelRoute = modelRoute;
    }

    public List<AITaskVarBinding> getVarBindings() {
        return varBindings;
    }

    public void setVarBindings(List<AITaskVarBinding> varBindings) {
        this.varBindings = varBindings;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Integer getDefaultPriority() {
        return defaultPriority;
    }

    public void setDefaultPriority(Integer defaultPriority) {
        this.defaultPriority = defaultPriority;
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

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public boolean isEnableRag() {
        return enableRag;
    }

    public void setEnableRag(boolean enableRag) {
        this.enableRag = enableRag;
    }
}

