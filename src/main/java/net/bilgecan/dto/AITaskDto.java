package net.bilgecan.dto;

public class AITaskDto {
    private Long id;
    private String name;
    private String prompt;
    private String systemPrompt;
    private String outputVarName;
    private String modelRoute;
    private String jsonSchema;
    private String inputFilePath;
    private String inputFileMimeType;
    private boolean enableRag = Boolean.FALSE;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isEnableRag() {
        return enableRag;
    }

    public void setEnableRag(boolean enableRag) {
        this.enableRag = enableRag;
    }
}
