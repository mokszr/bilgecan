package net.bilgecan.dto;

public class FileProcessingPipelineDto {
    private Long id;
    private String name;
    private InputSourceDto inputSource = new InputSourceDto();
    private OutputTargetDto outputTarget = new OutputTargetDto();
    private Long aiTaskTemplateId;
    private String aiTaskTemplateName;

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

    public InputSourceDto getInputSource() {
        return inputSource;
    }

    public void setInputSource(InputSourceDto inputSource) {
        this.inputSource = inputSource;
    }

    public OutputTargetDto getOutputTarget() {
        return outputTarget;
    }

    public void setOutputTarget(OutputTargetDto outputTarget) {
        this.outputTarget = outputTarget;
    }

    public Long getAiTaskTemplateId() {
        return aiTaskTemplateId;
    }

    public void setAiTaskTemplateId(Long aiTaskTemplateId) {
        this.aiTaskTemplateId = aiTaskTemplateId;
    }

    public String getAiTaskTemplateName() {
        return aiTaskTemplateName;
    }

    public void setAiTaskTemplateName(String aiTaskTemplateName) {
        this.aiTaskTemplateName = aiTaskTemplateName;
    }
}
