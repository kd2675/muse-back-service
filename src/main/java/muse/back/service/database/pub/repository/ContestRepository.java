package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.Contest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContestRepository extends JpaRepository<Contest, Long> {

    List<Contest> findAllByOrderByContestIdAsc();

    List<Contest> findByStatusOrderByDaysLeftAsc(String status);
}
