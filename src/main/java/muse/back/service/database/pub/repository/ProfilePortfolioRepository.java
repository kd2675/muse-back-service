package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.ProfilePortfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfilePortfolioRepository extends JpaRepository<ProfilePortfolio, Long> {

    List<ProfilePortfolio> findByArtistIdOrderByPortfolioIdAsc(Long artistId);
}
