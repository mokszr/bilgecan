package net.bilgecan.util;

import net.bilgecan.entity.AIResponseDetails;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;

import java.time.Duration;

public class AITaskRunnerUtility {

    public static AIResponseDetails toResponseDetails(ChatResponse chatRsp) {
        if (chatRsp == null) {
            return null;
        }
        AIResponseDetails responseDetails = new AIResponseDetails();

        responseDetails.setOutputTextAi(chatRsp.getResult().getOutput().getText());

        ChatResponseMetadata metadata = chatRsp.getMetadata();

        responseDetails.setDoneAi((Boolean) metadata.get("done"));
        responseDetails.setEvalDurationAi(getDurationInMillis(metadata, "eval-duration"));
        responseDetails.setLoadDurationAi(getDurationInMillis(metadata, "load-duration"));
        responseDetails.setTotalDurationAi(getDurationInMillis(metadata, "total-duration"));
        responseDetails.setPromptEvalDurationAi(getDurationInMillis(metadata, "prompt-eval-duration"));

        Usage usage = metadata.getUsage();
        responseDetails.setCompletionTokensAi(usage.getCompletionTokens());
        responseDetails.setPromptTokensAi(usage.getPromptTokens());
        responseDetails.setTotalTokensAi(usage.getTotalTokens());
        responseDetails.setFinishReasonAi(chatRsp.getResult().getMetadata().getFinishReason());

        return responseDetails;
    }

    private static Long getDurationInMillis(ChatResponseMetadata outputMetadata, String key) {
        Duration duration = (Duration) outputMetadata.get(key);
        return duration != null ? duration.toMillis() : null;
    }
}
