package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.ContestResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestResultRepository extends JpaRepository<ContestResult, Long> {
}
