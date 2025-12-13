package net.bilgecan.repository;

import net.bilgecan.entity.AITaskRun;
import org.springframework.ai.chat.model.ChatResponse;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

public interface AITaskRunRepositoryCustom {

    Optional<AITaskRun> claimNext(String owner, Duration lease, Set<Long> alreadyClaimedOnes);
    int extendLease(long id, String owner, Duration lease);
    int ack(long id, String owner, ChatResponse chatResponse);
    int cancel(long id);
    int fail(long id, String owner, String error);

}
