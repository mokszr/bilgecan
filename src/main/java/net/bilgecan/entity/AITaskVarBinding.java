package net.bilgecan.entity;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "ai_task_var_binding",
       uniqueConstraints = @UniqueConstraint(columnNames = {"ai_task_id","var_name"}))
public class AITaskVarBinding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_task_id", nullable = false)
    private AITaskTemplate task;

    @Column(name = "var_name", nullable = false)
    private String varName; // must match a template variable name of task.prompt

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "source", nullable = false)
    private VariableSource source = VariableSource.CONTEXT;

    // If source == LITERAL, use this text value
    @Column(name = "literal_value", columnDefinition = "text")
    private String literalValue;

    // If source == CONTEXT, pull from this key in the execution context
    @Column(name = "context_key")
    private String contextKey;

    // Optional: fallback literal if context key missing
    @Column(name = "fallback_literal", columnDefinition = "text")
    private String fallbackLiteral;

    // Optional: mark required → fail execution if unresolved
    @Column(name = "required", nullable = false)
    private boolean required = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AITaskTemplate getTask() {
        return task;
    }

    public void setTask(AITaskTemplate task) {
        this.task = task;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public VariableSource getSource() {
        return source;
    }

    public void setSource(VariableSource source) {
        this.source = source;
    }

    public String getLiteralValue() {
        return literalValue;
    }

    public void setLiteralValue(String literalValue) {
        this.literalValue = literalValue;
    }

    public String getContextKey() {
        return contextKey;
    }

    public void setContextKey(String contextKey) {
        this.contextKey = contextKey;
    }

    public String getFallbackLiteral() {
        return fallbackLiteral;
    }

    public void setFallbackLiteral(String fallbackLiteral) {
        this.fallbackLiteral = fallbackLiteral;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}

