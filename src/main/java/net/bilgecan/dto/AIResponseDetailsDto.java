package net.bilgecan.dto;


public class AIResponseDetailsDto {

    private String outputTextAi;
    private Integer promptTokensAi;
    private Integer completionTokensAi;
    private Integer totalTokensAi;
    private String finishReasonAi;
    private Long totalDurationAi;
    private Long promptEvalDurationAi;
    private Long evalDurationAi;
    private Long loadDurationAi;
    private Boolean doneAi;

    public Integer getPromptTokensAi() {
        return promptTokensAi;
    }

    public void setPromptTokensAi(Integer promptTokensAi) {
        this.promptTokensAi = promptTokensAi;
    }

    public Integer getCompletionTokensAi() {
        return completionTokensAi;
    }

    public void setCompletionTokensAi(Integer completionTokensAi) {
        this.completionTokensAi = completionTokensAi;
    }

    public Integer getTotalTokensAi() {
        return totalTokensAi;
    }

    public void setTotalTokensAi(Integer totalTokensAi) {
        this.totalTokensAi = totalTokensAi;
    }

    public String getFinishReasonAi() {
        return finishReasonAi;
    }

    public void setFinishReasonAi(String finishReasonAi) {
        this.finishReasonAi = finishReasonAi;
    }

    public Long getTotalDurationAi() {
        return totalDurationAi;
    }

    public void setTotalDurationAi(Long totalDurationAi) {
        this.totalDurationAi = totalDurationAi;
    }

    public Long getPromptEvalDurationAi() {
        return promptEvalDurationAi;
    }

    public void setPromptEvalDurationAi(Long promptEvalDurationAi) {
        this.promptEvalDurationAi = promptEvalDurationAi;
    }

    public Long getEvalDurationAi() {
        return evalDurationAi;
    }

    public void setEvalDurationAi(Long evalDurationAi) {
        this.evalDurationAi = evalDurationAi;
    }

    public Long getLoadDurationAi() {
        return loadDurationAi;
    }

    public void setLoadDurationAi(Long loadDurationAi) {
        this.loadDurationAi = loadDurationAi;
    }

    public Boolean getDoneAi() {
        return doneAi;
    }

    public void setDoneAi(Boolean doneAi) {
        this.doneAi = doneAi;
    }

    public String getOutputTextAi() {
        return outputTextAi;
    }

    public void setOutputTextAi(String outputTextAi) {
        this.outputTextAi = outputTextAi;
    }
}
