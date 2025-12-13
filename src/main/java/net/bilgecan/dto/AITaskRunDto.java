package net.bilgecan.dto;

import net.bilgecan.entity.AITaskStatus;

import java.time.OffsetDateTime;

public class AITaskRunDto {
    private Long id;
    private String resolvedPrompt;
    private String resolvedSystemPrompt;
    private AITaskStatus status;
    private String templateName;
    private String modelRoute;
    private String error;
    private AIResponseDetailsDto aiResponseDetails;
    private OffsetDateTime startedAt;
    private OffsetDateTime finishedAt;
    private String jsonSchema;
    private String inputFilePath;
    private String inputFileMimeType;
    private String leaseOwner;
    private boolean enableRag = Boolean.FALSE;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResolvedPrompt() {
        return resolvedPrompt;
    }

    public void setResolvedPrompt(String resolvedPrompt) {
        this.resolvedPrompt = resolvedPrompt;
    }

    public AITaskStatus getStatus() {
        return status;
    }

    public void setStatus(AITaskStatus status) {
        this.status = status;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public AIResponseDetailsDto getAiResponseDetails() {
        return aiResponseDetails;
    }

    public void setAiResponseDetails(AIResponseDetailsDto aiResponseDetails) {
        this.aiResponseDetails = aiResponseDetails;
    }

    public String getResolvedSystemPrompt() {
        return resolvedSystemPrompt;
    }

    public void setResolvedSystemPrompt(String resolvedSystemPrompt) {
        this.resolvedSystemPrompt = resolvedSystemPrompt;
    }

    public String getModelRoute() {
        return modelRoute;
    }

    public void setModelRoute(String modelRoute) {
        this.modelRoute = modelRoute;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
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

    public String getLeaseOwner() {
        return leaseOwner;
    }

    public void setLeaseOwner(String leaseOwner) {
        this.leaseOwner = leaseOwner;
    }

    public boolean isEnableRag() {
        return enableRag;
    }

    public void setEnableRag(boolean enableRag) {
        this.enableRag = enableRag;
    }
}
