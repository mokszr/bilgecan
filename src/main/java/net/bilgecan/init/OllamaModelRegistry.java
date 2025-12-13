package net.bilgecan.init;

import net.bilgecan.dto.OllamaTagsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OllamaModelRegistry implements ApplicationRunner {

    private final RestClient restClient;
    private final Set<String> models = ConcurrentHashMap.newKeySet();

    private final String defaultModel;

    public OllamaModelRegistry(
            @Value("${spring.ai.ollama.base-url}") String baseUrl,
            @Value("${spring.ai.ollama.chat.model}") String defaultModel) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.defaultModel = defaultModel;
    }

    @Override
    public void run(ApplicationArguments args) {
        refresh(); // load once on startup
    }

    /** Call Ollama /api/tags and refresh the in-memory set */
    @Scheduled(fixedDelayString = "PT10M") // refresh every 10 min
    public void refresh() {
        try {
            var resp = restClient.method(HttpMethod.GET)
                    .uri("/api/tags")
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new IllegalStateException("Ollama /api/tags failed with " + res.getStatusCode());
                    })
                    .body(OllamaTagsResponse.class);

            models.clear();
            if (resp != null && resp.models() != null) {
                resp.models().forEach(m -> models.add(m.name()));
            }
        } catch (Exception e) {

            // Don’t crash the app if Ollama is down; keep previous list
            e.printStackTrace();
            System.err.println("WARN: Could not refresh Ollama models: " + e.getMessage());

        }
    }

    /** Immutable, sorted view for consumers */
    public List<String> getModels() {
        return models.stream().sorted().toList();
    }

    public boolean exists(String name) {
        return models.contains(name);
    }

    /** If preferred is unavailable, fall back to configured default or any available model */
    public String pickDefault(String preferred) {
        if (preferred != null && exists(preferred)) return preferred;
        if (exists(defaultModel)) return defaultModel;
        return models.stream().findFirst().orElse(defaultModel);
    }
}
