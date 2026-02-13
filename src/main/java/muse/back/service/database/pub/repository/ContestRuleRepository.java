package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.ContestRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContestRuleRepository extends JpaRepository<ContestRule, Long> {

    List<ContestRule> findByContestIdOrderBySortOrderAsc(Long contestId);
}
