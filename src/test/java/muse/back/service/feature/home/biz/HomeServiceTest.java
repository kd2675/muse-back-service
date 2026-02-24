package muse.back.service.feature.home.biz;

import muse.back.service.database.pub.dto.HomeResponse;
import muse.back.service.database.pub.repository.ArtworkRepository;
import muse.back.service.database.pub.repository.ContestRepository;
import muse.back.service.database.pub.repository.GalleryCategoryRepository;
import muse.back.service.database.pub.repository.HomeHeroRepository;
import muse.back.service.database.pub.repository.HomePickRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeServiceTest {

    @Mock
    private HomeHeroRepository homeHeroRepository;
    @Mock
    private HomePickRepository homePickRepository;
    @Mock
    private ArtworkRepository artworkRepository;
    @Mock
    private GalleryCategoryRepository galleryCategoryRepository;
    @Mock
    private ContestRepository contestRepository;

    @InjectMocks
    private HomeService homeService;

    @Test
    void getHome_returnsFallbackHero_whenHeroDataMissing() {
        when(homeHeroRepository.findTopByOrderByHomeHeroIdDesc()).thenReturn(Optional.empty());
        when(homePickRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of());
        when(artworkRepository.findAllById(any())).thenReturn(List.of());
        when(artworkRepository.findCategoryArtworkCounts()).thenReturn(List.of());
        when(galleryCategoryRepository.findAllByOrderByCategoryKeyAsc()).thenReturn(List.of());
        when(contestRepository.findAllByOrderByContestIdAsc()).thenReturn(List.of());

        HomeResponse response = homeService.getHome();

        assertThat(response.hero().badge()).isEqualTo("MUSE HOME");
        assertThat(response.todaysPick()).isEmpty();
        assertThat(response.galleryCategories()).isEmpty();
        assertThat(response.activeContests()).isEmpty();
    }
}
