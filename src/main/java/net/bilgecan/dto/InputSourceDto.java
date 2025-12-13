package net.bilgecan.dto;

import net.bilgecan.entity.InputSourceType;
import java.util.HashMap;
import java.util.Map;


public class InputSourceDto {

    private Long id;

    private InputSourceType type;

    private Map<String, String> configs = new HashMap<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, String> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, String> configs) {
        this.configs = configs;
    }

    public InputSourceType getType() {
        return type;
    }

    public void setType(InputSourceType type) {
        this.type = type;
    }
}
