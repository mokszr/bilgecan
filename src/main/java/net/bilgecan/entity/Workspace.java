package net.bilgecan.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "workspace",
       uniqueConstraints = @UniqueConstraint(columnNames = {"slug"}))
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // URL-friendly, globally unique since we’re single-tenant
    @Column(nullable = false)
    private String slug;

    // lightweight settings as K/V
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "workspace_settings", joinColumns = @JoinColumn(name = "workspace_id"))
    @MapKeyColumn(name = "setting_key")
    @Column(name = "setting_value", columnDefinition = "text")
    private Map<String, String> settings = new HashMap<>();

    @Column(nullable = false)
    private boolean enabled = Boolean.TRUE;

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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
