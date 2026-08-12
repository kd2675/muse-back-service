package muse.back.service.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse.back.service.database.pub.entity.ImageCleanupTask;
import muse.back.service.database.pub.repository.ImageCleanupTaskRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageCleanupService {
    private static final int MAX_BACKOFF_HOURS = 24;

    private final ImageCleanupTaskRepository imageCleanupTaskRepository;
    private final ImageFinalizeClient imageFinalizeClient;
    private final TransactionTemplate transactionTemplate;

    @Value("${integration.image.cleanup-enabled:true}")
    private boolean cleanupEnabled;

    @Transactional
    public void enqueue(String fileName, String reason) {
        enqueueInternal(fileName, reason, false);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enqueueCompensation(String fileName, String reason) {
        enqueueInternal(fileName, reason, true);
    }

    private void enqueueInternal(String fileName, String reason, boolean compensation) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }
        if (!cleanupEnabled) {
            if (!compensation && TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        imageFinalizeClient.deleteImage(fileName);
                    }
                });
            } else {
                imageFinalizeClient.deleteImage(fileName);
            }
            return;
        }
        if (imageCleanupTaskRepository.existsByFileName(fileName)) {
            return;
        }
        imageCleanupTaskRepository.save(new ImageCleanupTask(fileName, reason, LocalDateTime.now()));
    }

    @Scheduled(
            fixedDelayString = "${integration.image.cleanup-delay-ms:60000}",
            initialDelayString = "${integration.image.cleanup-initial-delay-ms:60000}"
    )
    public void processDueTasks() {
        if (!cleanupEnabled) {
            return;
        }
        List<ImageCleanupTask> tasks = imageCleanupTaskRepository
                .findTop50ByNextAttemptAtLessThanEqualOrderByTaskIdAsc(LocalDateTime.now());
        tasks.forEach(task -> transactionTemplate.executeWithoutResult(status -> processTask(task)));
    }

    private void processTask(ImageCleanupTask task) {
        try {
            imageFinalizeClient.deleteImage(task.getFileName());
            imageCleanupTaskRepository.deleteById(task.getTaskId());
        } catch (RuntimeException exception) {
            int backoffHours = Math.min(1 << Math.min(task.getAttemptCount(), 4), MAX_BACKOFF_HOURS);
            task.retry(LocalDateTime.now().plusHours(backoffHours), exception.getMessage());
            imageCleanupTaskRepository.save(task);
            log.warn(
                    "Muse image cleanup deferred: fileName={}, attempt={}",
                    task.getFileName(),
                    task.getAttemptCount(),
                    exception
            );
        }
    }
}
