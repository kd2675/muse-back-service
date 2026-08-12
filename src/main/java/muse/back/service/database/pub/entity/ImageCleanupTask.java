package muse.back.service.database.pub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "image_cleanup_task",
        uniqueConstraints = @UniqueConstraint(name = "uk_image_cleanup_task_file", columnNames = "file_name")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageCleanupTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "reason", nullable = false, length = 40)
    private String reason;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "next_attempt_at", nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "create_date", nullable = false, updatable = false)
    private LocalDateTime createDate;

    public ImageCleanupTask(String fileName, String reason, LocalDateTime now) {
        this.fileName = fileName;
        this.reason = reason;
        this.attemptCount = 0;
        this.nextAttemptAt = now;
        this.createDate = now;
    }

    public void retry(LocalDateTime nextAttemptAt, String lastError) {
        this.attemptCount += 1;
        this.nextAttemptAt = nextAttemptAt;
        this.lastError = lastError == null ? null : lastError.substring(0, Math.min(lastError.length(), 500));
    }
}
