package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.HomeHero;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HomeHeroRepository extends JpaRepository<HomeHero, Long> {

    Optional<HomeHero> findTopByOrderByHomeHeroIdDesc();
}
