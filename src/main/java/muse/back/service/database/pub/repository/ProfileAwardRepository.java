package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.ProfileAward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfileAwardRepository extends JpaRepository<ProfileAward, Long> {

    List<ProfileAward> findByArtistIdOrderByAwardIdAsc(Long artistId);
}
