package net.bilgecan.dto;

import net.bilgecan.entity.PromptType;

public class PromptDto {

    private Long id;
    private String name;
    private String input;
    private PromptType type = PromptType.USER;

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

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public PromptType getType() {
        return type;
    }

    public void setType(PromptType type) {
        this.type = type;
    }

}
