package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.Museum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MuseumRepository extends JpaRepository<Museum, Long> {

    List<Museum> findAllByOrderByMuseumIdDesc();

    List<Museum> findByArtistIdOrderByMuseumIdDesc(Long artistId);

    List<Museum> findByIsPublicTrueOrderByMuseumIdDesc();

    Optional<Museum> findByMuseumIdAndArtistId(Long museumId, Long artistId);

    List<Museum> findTop20ByIsPublicTrueAndNameContainingIgnoreCaseOrderByMuseumIdDesc(String name);
}
