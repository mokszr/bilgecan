package net.bilgecan.dto;

import net.bilgecan.entity.OutputTargetType;
import java.util.HashMap;
import java.util.Map;

public class OutputTargetDto {

    private Long id;
    private OutputTargetType type;
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

    public OutputTargetType getType() {
        return type;
    }

    public void setType(OutputTargetType type) {
        this.type = type;
    }
}
