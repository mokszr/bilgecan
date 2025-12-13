package net.bilgecan.dto;

import java.util.List;

public record OllamaTagsResponse(List<Model> models) {
    public record Model(String name) {}
}