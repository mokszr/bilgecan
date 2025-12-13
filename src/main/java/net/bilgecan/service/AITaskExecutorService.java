package net.bilgecan.service;

import net.bilgecan.entity.AITaskRun;
import net.bilgecan.entity.AITaskStatus;
import net.bilgecan.pipeline.fileprocessing.OutputWriterService;
import net.bilgecan.repository.AITaskRunRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AITaskExecutorService implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(AITaskExecutorService.class);

    // Tune these for your box (CPU/GPU concurrency)
    @Value("${bilgecan.maxConcurrentAITaskExecution:1}")
    private int maxConcurrent;
    private final Duration claimLease = Duration.ofSeconds(300);     // lock timeout
    private final Duration heartbeatEvery = Duration.ofSeconds(20);  // renew before expiry

    private final String workerId = "bilgecan-" + UUID.randomUUID();
    private final AITaskRunRepository repo;
    private final AITaskRunService aiTaskRunService;
    private final TaskScheduler scheduler;
    private ExecutorService pool;
    private final ConcurrentHashMap<Long, Boolean> claimedTasks = new ConcurrentHashMap<>();
    private AITaskRunner aiTaskRunner;
    private OutputWriterService outputWriterService;

    public AITaskExecutorService(AITaskRunRepository repo, TaskScheduler scheduler, AITaskRunner aiTaskRunner, AITaskRunService aiTaskRunService, OutputWriterService outputWriterService) {
        this.repo = repo;
        this.scheduler = scheduler;
        this.aiTaskRunner = aiTaskRunner;
        this.aiTaskRunService = aiTaskRunService;
        this.outputWriterService = outputWriterService;

    }

    @PostConstruct
    public void init() {
        this.pool = new ThreadPoolExecutor(
                1, maxConcurrent,
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(maxConcurrent),
                r -> {
                    Thread t = new Thread(r, "bilgecan-worker");
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.DiscardPolicy()   // backpressure if queue is full
        );
        log.info("AI Task Executor started with concurrency={}", maxConcurrent);
    }

    // Light polling loop; this only CLAIMS. Actual work runs in pool.
    @Scheduled(fixedDelay = 15000)
    public void pollAndDispatch() {
        // Don’t flood the pool
        if (getRemainingCapacity() == 0) {
            log.info("thread pool is full, skipping ");
            return;
        }

        log.info("thread pool queue remainingCapacity " + getRemainingCapacity() +
                " getActiveCount " + getActiveCount());

        repo.claimNext(workerId, claimLease, new HashSet<>(claimedTasks.keySet())).ifPresent(task -> {
            log.info("Claimed, to be run task found " + task.getId());
            claimedTasks.put(task.getId(), true);
            pool.submit(() -> executeOne(task.getId()));
        });
    }



    private void executeOne(Long taskId) {
        AtomicBoolean running = new AtomicBoolean(true);
        ScheduledFuture<?> heartbeat = null;
        try {
            Optional<AITaskRun> taskByIdOptional = repo.findById(taskId);
            if (taskByIdOptional.isEmpty() ||
                    taskByIdOptional.get().getStatus().equals(AITaskStatus.CANCELED) || // task is canceled
                    taskByIdOptional.get().getStatus().equals(AITaskStatus.DONE) || // task is already executed and done in another worker
                    !workerId.equals(taskByIdOptional.get().getLeaseOwner())) { // another worker claimed this task
                log.info("Task #{} execution is skipped, no need to execute ", taskId);
                return;
            }
            AITaskRun task = taskByIdOptional.get();

            // Heartbeat (lease extension)
            heartbeat = scheduler.scheduleAtFixedRate(
                    () -> safeExtendLease(taskId, running),
                    heartbeatEvery
            );

            ChatResponse chatResponse = runAi(task);// throws for retry/fail

            // On success
            repo.ack(taskId, workerId, chatResponse);

            if (task.getFileProcessingPipeline() != null) {
                outputWriterService.writeOutput(aiTaskRunService.findById(task.getId()), task.getFileProcessingPipeline().getOutputTarget());
            }

            log.info("Task #{} DONE", taskId);

        } catch (Exception e) {
            repo.fail(taskId, workerId, e + " " + e.getMessage());
            log.error("Task #{} unexpected error", taskId, e);
        } finally {
            running.set(false);
            claimedTasks.remove(taskId);
            if (heartbeat != null) heartbeat.cancel(true);
        }
    }

    private void safeExtendLease(long id, AtomicBoolean running) {
        try {
            if (running.get()) {
                int updated = repo.extendLease(id, workerId, claimLease);
                if (updated == 0) {
                    log.info("Lease extend skipped for task #{} (already finished)", id);
                } else {
                    log.info("Lease extended successfully for task #{}", id);
                }
            }
        } catch (Exception e) {
            log.warn("Lease extend failed for task #{}: {}", id, e.toString());
        }
    }


    private ChatResponse runAi(AITaskRun task) throws Exception {
        log.info("runAi execution is starting taskRun id: " + task.getId());

        return aiTaskRunner.run(task);
    }

    public int getActiveCount() {
        return ((ThreadPoolExecutor) pool).getActiveCount();
    }

    public int getRemainingCapacity() {
        return ((ThreadPoolExecutor) pool).getQueue().remainingCapacity();
    }

    @Override
    public void destroy() {
        log.info("Shutting down AI Task Executor");
        pool.shutdown();
        try {
            if (!pool.awaitTermination(20, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public String getWorkerId() {
        return workerId;
    }
}
