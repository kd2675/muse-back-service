package muse.back.service.feature.gallery.biz;

import muse.back.service.common.util.ImageFileUrlResolver;
import muse.back.service.database.pub.entity.Museum;
import muse.back.service.database.pub.entity.MuseumViewHistory;
import muse.back.service.database.pub.entity.ProfileArtist;
import muse.back.service.database.pub.repository.MuseumArtworkRepository;
import muse.back.service.database.pub.repository.MuseumBookmarkRepository;
import muse.back.service.database.pub.repository.MuseumRepository;
import muse.back.service.database.pub.repository.MuseumViewHistoryRepository;
import muse.back.service.database.pub.repository.ProfileArtistRepository;
import muse.back.service.feature.profile.biz.ArtistIdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GalleryEngagementServiceTest {
    @Mock private ArtistIdentityService artistIdentityService;
    @Mock private MuseumRepository museumRepository;
    @Mock private MuseumArtworkRepository artworkRepository;
    @Mock private MuseumBookmarkRepository bookmarkRepository;
    @Mock private MuseumViewHistoryRepository viewHistoryRepository;
    @Mock private ProfileArtistRepository profileArtistRepository;
    @Mock private ImageFileUrlResolver imageFileUrlResolver;
    @InjectMocks private GalleryEngagementService galleryEngagementService;

    @Test
    void getHistory_museumWasUnpublished_doesNotExposePrivateMuseum() {
        Museum unpublishedMuseum = mock(Museum.class);
        MuseumViewHistory history = new MuseumViewHistory(10L, 30L, null, 50);
        when(artistIdentityService.requireByUserKey("user-1"))
                .thenReturn(new ProfileArtist(10L, "user-1", "Viewer", null, "#111111"));
        when(viewHistoryRepository.findTop30ByArtistIdOrderByViewedAtDesc(10L)).thenReturn(List.of(history));
        when(unpublishedMuseum.getMuseumId()).thenReturn(30L);
        when(unpublishedMuseum.isPublic()).thenReturn(false);
        when(museumRepository.findAllById(any())).thenReturn(List.of(unpublishedMuseum));

        var response = galleryEngagementService.getHistory("user-1");

        assertThat(response).isEmpty();
    }
}
