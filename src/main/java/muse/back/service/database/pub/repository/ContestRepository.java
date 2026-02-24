package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.Contest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContestRepository extends JpaRepository<Contest, Long> {

    List<Contest> findAllByOrderByContestIdAsc();

    Optional<Contest> findTopByOrderByContestIdDesc();
}
