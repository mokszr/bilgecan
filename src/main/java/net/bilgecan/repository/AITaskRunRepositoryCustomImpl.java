package net.bilgecan.repository;

import net.bilgecan.entity.AIResponseDetails;
import net.bilgecan.entity.AITaskRun;
import net.bilgecan.entity.AITaskStatus;
import net.bilgecan.util.AITaskRunnerUtility;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@Transactional
public class AITaskRunRepositoryCustomImpl implements AITaskRunRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<AITaskRun> claimNext(String owner, Duration lease, Set<Long> alreadyClaimedOnes) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime newLeaseUntil = now.plus(lease);

        String parametrizedQuery = """
                SELECT r FROM AITaskRun r
                WHERE (r.status = :pendingStatus)
                   OR (r.status = :runningStatus AND (r.leaseUntil IS NULL OR r.leaseUntil < :now))
                ORDER BY r.priority ASC, r.createdAt ASC
                """;

        if (!alreadyClaimedOnes.isEmpty()) {
            parametrizedQuery = """
                    SELECT r FROM AITaskRun r
                    WHERE ((r.status = :pendingStatus)
                       OR (r.status = :runningStatus AND (r.leaseUntil IS NULL OR r.leaseUntil < :now)))
                       AND r.id NOT IN :alreadyClaimedOnes
                    ORDER BY r.priority ASC, r.createdAt ASC
                    """;
        }

        // 1. Find one candidate (status PENDING or expired RUNNING)
        TypedQuery<AITaskRun> aiTaskRunTypedQuery = em.createQuery(parametrizedQuery, AITaskRun.class)
                .setParameter("now", now)
                .setParameter("pendingStatus", AITaskStatus.PENDING)
                .setParameter("runningStatus", AITaskStatus.RUNNING);

        if (!alreadyClaimedOnes.isEmpty()) {
            aiTaskRunTypedQuery.setParameter("alreadyClaimedOnes", alreadyClaimedOnes);
        }

        List<AITaskRun> candidates = aiTaskRunTypedQuery
                .setMaxResults(1)
                .getResultList();

        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        AITaskRun run = candidates.get(0);
        long oldVersion = run.getVersion() == null ? 0L : run.getVersion();

        // 2. Try atomic claim with optimistic condition
        int updated = em.createQuery("""
                        UPDATE AITaskRun r
                        SET r.status = :runningStatus,
                            r.leaseOwner = :owner,
                            r.leaseUntil = :leaseUntil,
                            r.startedAt = :now,
                            r.version = r.version + 1
                        WHERE r.id = :id
                          AND (r.status = :pendingStatus OR (r.status = :runningStatus AND (r.leaseUntil IS NULL OR r.leaseUntil < :now)))
                          AND (r.version = :oldVersion OR r.version IS NULL)
                        """).setParameter("pendingStatus", AITaskStatus.PENDING)
                .setParameter("runningStatus", AITaskStatus.RUNNING)
                .setParameter("owner", owner)
                .setParameter("leaseUntil", newLeaseUntil)
                .setParameter("id", run.getId())
                .setParameter("now", now)
                .setParameter("oldVersion", oldVersion)
                .executeUpdate();

        if (updated == 1) {
            // Claim successful — reload entity
            AITaskRun claimed = em.find(AITaskRun.class, run.getId());
            return Optional.of(claimed);
        }

        return Optional.empty();
    }

    @Override
    public int extendLease(long id, String owner, Duration lease) {
        OffsetDateTime newLeaseUntil = OffsetDateTime.now().plus(lease);
        return em.createQuery("""
                        UPDATE AITaskRun r
                        SET r.leaseUntil = :leaseUntil, r.version = r.version + 1
                        WHERE r.id = :id AND r.leaseOwner = :owner AND r.status = :runningStatus
                        """)
                .setParameter("runningStatus", AITaskStatus.RUNNING)
                .setParameter("leaseUntil", newLeaseUntil)
                .setParameter("id", id)
                .setParameter("owner", owner)
                .executeUpdate();
    }

    @Override
    public int ack(long id, String leaseOwner, ChatResponse chatResponse) {
        AIResponseDetails aiResponseDetails = AITaskRunnerUtility.toResponseDetails(chatResponse);
        OffsetDateTime now = OffsetDateTime.now();
        return em.createQuery("""
                        UPDATE AITaskRun r
                        SET r.status = :doneStatus,
                            r.leaseOwner = NULL,
                            r.leaseUntil = NULL,
                            r.finishedAt = :now,
                            r.aiResponseDetails = :aiResponseDetails,
                            r.version = r.version + 1
                        WHERE r.id = :id AND r.leaseOwner = :leaseOwner
                        """)
                .setParameter("doneStatus", AITaskStatus.DONE)
                .setParameter("id", id)
                .setParameter("leaseOwner", leaseOwner)
                .setParameter("now", now)
                .setParameter("aiResponseDetails", aiResponseDetails)
                .executeUpdate();
    }

    @Override
    public int cancel(long id) {
        return em.createQuery("""
                        UPDATE AITaskRun r
                        SET r.status = :cancelStatus,
                            r.leaseOwner = NULL,
                            r.leaseUntil = NULL,
                            r.version = r.version + 1
                        WHERE r.id = :id AND (r.status = :runningStatus OR r.status = :pendingStatus)
                        """)
                .setParameter("cancelStatus", AITaskStatus.CANCELED)
                .setParameter("runningStatus", AITaskStatus.RUNNING)
                .setParameter("pendingStatus", AITaskStatus.PENDING)
                .setParameter("id", id)
                .executeUpdate();
    }

    @Override
    public int fail(long id, String owner, String error) {
        OffsetDateTime now = OffsetDateTime.now();

        return em.createQuery("""
                        UPDATE AITaskRun r
                        SET r.status = :failedStatus,
                            r.error = :error,
                            r.leaseOwner = NULL,
                            r.leaseUntil = NULL,
                            r.finishedAt = :now,
                            r.version = r.version + 1
                        WHERE r.id = :id AND r.leaseOwner = :owner AND r.status != :canceledStatus
                        """)
                .setParameter("failedStatus", AITaskStatus.FAILED)
                .setParameter("canceledStatus", AITaskStatus.CANCELED)
                .setParameter("error", truncate(error, 3000))
                .setParameter("id", id)
                .setParameter("owner", owner)
                .setParameter("now", now)
                .executeUpdate();
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
