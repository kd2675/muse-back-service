package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.ImageCleanupTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ImageCleanupTaskRepository extends JpaRepository<ImageCleanupTask, Long> {
    List<ImageCleanupTask> findTop50ByNextAttemptAtLessThanEqualOrderByTaskIdAsc(LocalDateTime now);

    boolean existsByFileName(String fileName);
}
