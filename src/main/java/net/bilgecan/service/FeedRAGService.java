package net.bilgecan.service;

import net.bilgecan.dto.FeedRagHistoryDto;
import net.bilgecan.entity.FeedRagHistory;
import net.bilgecan.exception.AppLevelValidationException;
import net.bilgecan.repository.FeedRagHistoryRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Transactional(rollbackFor = Exception.class)
@Service
public class FeedRAGService {

    private static final Logger log = LoggerFactory.getLogger(FeedRAGService.class);
    public static final String DOCUMENT_UNIQUE_ID = "documentUniqueId";

    private final VectorStore vectorStore;
    private final FeedRagHistoryRepository feedRagHistoryRepository;

    public record FeedResult(String name, boolean success, String error) {
    }

    public FeedRAGService(VectorStore vectorStore, FeedRagHistoryRepository feedRagHistoryRepository) {
        this.vectorStore = vectorStore;
        this.feedRagHistoryRepository = feedRagHistoryRepository;
    }

    public void delete(FeedRagHistoryDto dto) {
        FeedRagHistory history = feedRagHistoryRepository.findById(dto.getId())
                .orElseThrow(() -> new AppLevelValidationException("Feed history not found"));
        String documentUniqueId = history.getDocumentUniqueId();

        if (StringUtils.isNotBlank(documentUniqueId)) {
            FilterExpressionBuilder b = new FilterExpressionBuilder();
            var expr = b.eq(DOCUMENT_UNIQUE_ID, documentUniqueId).build();

            // This will delete all documents whose metadata.documentUniqueId == <documentUniqueId>
            vectorStore.delete(expr);
        }

        feedRagHistoryRepository.deleteById(dto.getId());
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<FeedRagHistoryDto> findPaginated(Pageable pageable) {
        Page<FeedRagHistory> entities = feedRagHistoryRepository.findAllByOrderByDateDesc(pageable);
        return mapToDto(entities);
    }

    private Page<FeedRagHistoryDto> mapToDto(Page<FeedRagHistory> page) {
        return page.map(this::toDto);
    }

    private FeedRagHistoryDto toDto(FeedRagHistory entity) {
        FeedRagHistoryDto dto = new FeedRagHistoryDto();
        dto.setId(entity.getId());
        dto.setDate(entity.getDate());
        dto.setFileName(entity.getFileName());
        dto.setMimeType(entity.getMimeType());
        dto.setSize(entity.getSize());

        return dto;
    }


    @PreAuthorize("hasRole('ADMIN')")
    public FeedResult feedFile(File tempFile, String fileName) {
        try {
            String mimeType = detectMimeType(tempFile);
            log.info("feedFile Detected MIME type: {}", mimeType);

            String documentUniqueId = UUID.randomUUID().toString();

            TikaDocumentReader reader = new TikaDocumentReader(new FileSystemResource(tempFile));

            List<Document> rawDocs = reader.read();
            List<Document> docsWithMeta = rawDocs.stream()
                    .map(d -> {
                        Map<String, Object> newMeta = new HashMap<>(d.getMetadata());
                        newMeta.put(DOCUMENT_UNIQUE_ID, documentUniqueId);
                        return new Document(d.getId(), d.getText(), newMeta);
                    })
                    .toList();

            TokenTextSplitter splitter = new TokenTextSplitter();
            vectorStore.accept(splitter.apply(docsWithMeta));
            log.info("VectorStore loaded with data from tempFile " + tempFile);

            FeedRagHistory history = new FeedRagHistory();
            history.setDate(OffsetDateTime.now());
            history.setFileName(fileName);
            history.setMimeType(mimeType);
            history.setSize(tempFile.length());
            history.setDocumentUniqueId(documentUniqueId);
            feedRagHistoryRepository.save(history);

            tempFile.delete();
            return new FeedResult(fileName, true, null);
        } catch (Exception e) {
            log.error("Feed failed", e);
            return new FeedResult(fileName, false, e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void feedPlainText(String text, String name) {
        if (StringUtils.isBlank(text) || StringUtils.isBlank(name)) {
            throw new AppLevelValidationException("Empty text cannot be feed");
        }

        String documentUniqueId = UUID.randomUUID().toString();

        Map<String, Object> meta = Map.of(
                DOCUMENT_UNIQUE_ID, documentUniqueId,
                "source", "user-plaintext",
                "name", name,
                "createdAt", java.time.OffsetDateTime.now().toString()
        );

        Document doc = new Document(text, meta);
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> chunks = splitter.apply(List.of(doc));
        vectorStore.accept(chunks);
        log.info("VectorStore loaded with data from user plain text ");

        FeedRagHistory history = new FeedRagHistory();
        history.setDate(OffsetDateTime.now());
        history.setFileName(name);
        history.setMimeType("N/A");
        history.setSize((long) text.length());
        history.setDocumentUniqueId(documentUniqueId);
        feedRagHistoryRepository.save(history);
    }

    public String detectMimeType(File file) {
        Tika tika = new Tika();
        try {
            return tika.detect(file);   // e.g., "application/pdf", "text/plain"
        } catch (IOException e) {
            return null;
        }
    }
}
