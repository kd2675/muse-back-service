package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.HomePick;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomePickRepository extends JpaRepository<HomePick, Long> {

    List<HomePick> findAllByOrderBySortOrderAsc();
}
