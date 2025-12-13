package net.bilgecan.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "output_target")
public class OutputTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OutputTargetType type;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "output_target_config",
            joinColumns = @JoinColumn(name = "output_target_id")
    )
    @MapKeyColumn(name = "config_key")
    @Column(name = "config_value", length = 2000)
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
