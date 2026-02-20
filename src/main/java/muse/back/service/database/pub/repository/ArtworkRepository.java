package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.Artwork;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArtworkRepository extends JpaRepository<Artwork, Long> {

    interface CategoryArtworkCount {
        String getCategoryKey();
        long getItemCount();
    }

    List<Artwork> findAllByOrderByArtworkIdDesc();

    List<Artwork> findByCategoryKeyOrderByArtworkIdAsc(String categoryKey);

    long countByCategoryKey(String categoryKey);

    @Query("""
            select a.categoryKey as categoryKey, count(a) as itemCount
            from Artwork a
            where a.categoryKey is not null
            group by a.categoryKey
            """)
    List<CategoryArtworkCount> findCategoryArtworkCounts();

    List<Artwork> findTop3ByCategoryKeyAndArtworkIdNotOrderByArtworkIdDesc(String categoryKey, Long artworkId);

    Artwork findTopByOrderByArtworkIdDesc();
}
