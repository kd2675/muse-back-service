package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.GalleryCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GalleryCategoryRepository extends JpaRepository<GalleryCategory, String> {

    List<GalleryCategory> findAllByOrderByItemCountDesc();
}
