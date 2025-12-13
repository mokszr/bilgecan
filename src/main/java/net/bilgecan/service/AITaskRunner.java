package net.bilgecan.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.bilgecan.entity.AITaskRun;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.content.Media;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

import java.io.File;
import java.util.Map;

@Service
public class AITaskRunner {

    private static final Logger log = LoggerFactory.getLogger(AITaskRunner.class);

    private final ChatClient chatClient;
    private final ChatClient chatClientRagAware;
    private String rootInputFileDirectoryPath;

    public AITaskRunner(ChatModel chatModel, VectorStore vectorStore) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.chatClientRagAware = ChatClient.builder(chatModel).defaultAdvisors(new QuestionAnswerAdvisor(vectorStore)).build();

    }

    public ChatResponse run(AITaskRun aiTaskRun) throws JsonProcessingException {
        ChatClient chatClientSelected = selectChatClient(aiTaskRun.isEnableRag());

        // specify model at runtime
        OllamaOptions.Builder optionsBuilder = OllamaOptions.builder()
                .model(aiTaskRun.getModelRoute());

        if (StringUtils.isNotBlank(aiTaskRun.getJsonSchema())) {
            optionsBuilder.format(new ObjectMapper().readValue(aiTaskRun.getJsonSchema(), Map.class));
        }

        ChatClient.ChatClientRequestSpec chatClientRequest = chatClientSelected.prompt()
                .options(optionsBuilder
                        .build())
                .user(u -> {
                    u.text(aiTaskRun.getResolvedPrompt());
                    if (StringUtils.isNotBlank(aiTaskRun.getInputFileMimeType())
                            && StringUtils.isNotBlank(aiTaskRun.getInputFilePath())) {
                        u.media(Media.builder()
                                .mimeType(MimeType.valueOf(aiTaskRun.getInputFileMimeType()))
                                .data(new FileSystemResource(new File(rootInputFileDirectoryPath + File.separator + aiTaskRun.getInputFilePath()))).build());
                    }
                });
        if (StringUtils.isNotBlank(aiTaskRun.getResolvedSystemPrompt())) {
            chatClientRequest = chatClientRequest.system(aiTaskRun.getResolvedSystemPrompt());
        }

        log.info("I'm running chat call from taskRun with id : " + aiTaskRun.getId());
        return chatClientRequest.call().chatResponse();
    }

    private ChatClient selectChatClient(Boolean enableRag) {
        if (enableRag) {
            return chatClientRagAware;
        }
        return chatClient;
    }

    @Value("${bilgecan.rootInputFileDirectoryPath}")
    public void setRootInputFileDirectoryPath(String rootInputFileDirectoryPath) {
        this.rootInputFileDirectoryPath = rootInputFileDirectoryPath;
    }
}
