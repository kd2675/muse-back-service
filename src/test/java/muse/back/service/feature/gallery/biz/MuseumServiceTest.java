package muse.back.service.feature.gallery.biz;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import muse.back.service.common.util.ImageCleanupService;
import muse.back.service.common.util.ImageFileUrlResolver;
import muse.back.service.common.util.ImageFinalizeClient;
import muse.back.service.database.pub.dto.AdminMuseumVisibilityUpdateRequest;
import muse.back.service.database.pub.dto.MyMuseumCreateRequest;
import muse.back.service.database.pub.dto.MyMuseumUpdateRequest;
import muse.back.service.database.pub.dto.MuseumArtworkUpdateRequest;
import muse.back.service.database.pub.dto.MuseumCurationUpdateRequest;
import muse.back.service.database.pub.entity.Museum;
import muse.back.service.database.pub.entity.MuseumArtwork;
import muse.back.service.database.pub.entity.ProfileArtist;
import muse.back.service.database.pub.repository.ArtistFollowRepository;
import muse.back.service.database.pub.repository.MuseumArtworkRepository;
import muse.back.service.database.pub.repository.MuseumRepository;
import muse.back.service.database.pub.repository.ProfileArtistRepository;
import muse.back.service.feature.notification.biz.NotificationService;
import web.common.core.response.base.exception.GeneralException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MuseumServiceTest {
    @Mock private MuseumRepository museumRepository;
    @Mock private MuseumArtworkRepository museumArtworkRepository;
    @Mock private ProfileArtistRepository profileArtistRepository;
    @Mock private ImageFinalizeClient imageFinalizeClient;
    @Mock private ImageFileUrlResolver imageFileUrlResolver;
    @Mock private ImageCleanupService imageCleanupService;
    @Mock private ArtistFollowRepository artistFollowRepository;
    @Mock private NotificationService notificationService;
    @InjectMocks private MuseumService museumService;

    @Test
    void createMyMuseum_validRequest_alwaysStartsAsDraft() {
        when(profileArtistRepository.findByUserKey("user-1"))
                .thenReturn(Optional.of(new ProfileArtist(10L, "user-1", "Artist", null, "#111111")));
        when(museumRepository.save(org.mockito.ArgumentMatchers.any(Museum.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = museumService.createMyMuseum(
                "user-1",
                new MyMuseumCreateRequest("Night Archive", "desc")
        );

        assertThat(response.publishStatus()).isEqualTo("DRAFT");
        assertThat(response.isPublic()).isFalse();
    }

    @Test
    void updateMyMuseum_publishedMuseum_preservesPublicationState() {
        Museum museum = new Museum(10L, "Night Archive", "desc", true, false);
        when(profileArtistRepository.findByUserKey("user-1"))
                .thenReturn(Optional.of(new ProfileArtist(10L, "user-1", "Artist", null, "#111111")));
        when(museumRepository.findByMuseumIdAndArtistId(1L, 10L)).thenReturn(Optional.of(museum));
        when(museumRepository.save(museum)).thenReturn(museum);

        var response = museumService.updateMyMuseum(
                1L,
                "user-1",
                new MyMuseumUpdateRequest("Renamed Archive", "new description")
        );

        assertThat(response.publishStatus()).isEqualTo("PUBLISHED");
        assertThat(response.isPublic()).isTrue();
        assertThat(response.name()).isEqualTo("Renamed Archive");
    }

    @Test
    void updateCuration_futureScheduleWithVisibleArtwork_makesScheduledMuseumPublic() {
        Museum museum = new Museum(10L, "Night Archive", "desc", false, false);
        when(profileArtistRepository.findByUserKey("user-1"))
                .thenReturn(Optional.of(new ProfileArtist(10L, "user-1", "Artist", null, "#111111")));
        when(museumRepository.findByMuseumIdAndArtistId(1L, 10L)).thenReturn(Optional.of(museum));
        when(museumArtworkRepository.countByMuseumIdAndModerationStatus(1L, "VISIBLE")).thenReturn(1L);
        when(museumRepository.save(museum)).thenReturn(museum);

        var response = museumService.updateCuration(
                1L,
                "user-1",
                new MuseumCurationUpdateRequest(
                        "SCHEDULED", null, LocalDateTime.now().plusDays(1), "opening note", "SALON", "WARM"
                )
        );

        assertThat(response.publishStatus()).isEqualTo("SCHEDULED");
        assertThat(response.isPublic()).isTrue();
        assertThat(response.curatorNote()).isEqualTo("opening note");
    }

    @Test
    void updateMyMuseumArtwork_audioWithoutTranscript_rejectsInaccessibleGuide() {
        Museum museum = new Museum(10L, "Night Archive", "desc", false, false);
        MuseumArtwork artwork = new MuseumArtwork(1L, 10L, "Work", "desc", "work.jpg", "VISIBLE");
        when(profileArtistRepository.findByUserKey("user-1"))
                .thenReturn(Optional.of(new ProfileArtist(10L, "user-1", "Artist", null, "#111111")));
        when(museumRepository.findByMuseumIdAndArtistId(1L, 10L)).thenReturn(Optional.of(museum));
        when(museumArtworkRepository.findByMuseumArtworkIdAndMuseumId(2L, 1L)).thenReturn(Optional.of(artwork));

        assertThrows(
                GeneralException.class,
                () -> museumService.updateMyMuseumArtwork(
                        1L,
                        2L,
                        "user-1",
                        new MuseumArtworkUpdateRequest(
                                "Work", "desc", 0, null, 50, 50,
                                "https://media.example.com/guide.mp3", null, "WARM"
                        )
                )
        );
    }

    @Test
    void updateAdminMuseumVisibility_noVisibleArtwork_rejectsEmptyPublication() {
        Museum museum = new Museum(10L, "Night Archive", "desc", false, false);
        when(museumRepository.findById(1L)).thenReturn(Optional.of(museum));
        when(museumArtworkRepository.countByMuseumIdAndModerationStatus(1L, "VISIBLE")).thenReturn(0L);

        assertThrows(
                GeneralException.class,
                () -> museumService.updateAdminMuseumVisibility(
                        1L,
                        new AdminMuseumVisibilityUpdateRequest(true)
                )
        );
    }

    @Test
    void deleteMyMuseumArtwork_lastVisibleArtwork_movesPublishedMuseumToDraft() {
        Museum museum = org.mockito.Mockito.mock(Museum.class);
        MuseumArtwork artwork = org.mockito.Mockito.mock(MuseumArtwork.class);
        when(profileArtistRepository.findByUserKey("user-1"))
                .thenReturn(Optional.of(new ProfileArtist(10L, "user-1", "Artist", null, "#111111")));
        when(museumRepository.findByMuseumIdAndArtistId(1L, 10L)).thenReturn(Optional.of(museum));
        when(museumArtworkRepository.findByMuseumArtworkIdAndMuseumId(2L, 1L)).thenReturn(Optional.of(artwork));
        when(museum.getMuseumId()).thenReturn(1L);
        when(museum.isPublic()).thenReturn(true);
        when(artwork.getMuseumArtworkId()).thenReturn(2L);
        when(artwork.getModerationStatus()).thenReturn("VISIBLE");
        when(artwork.getFileName()).thenReturn("work.jpg");
        when(museumArtworkRepository.countByMuseumIdAndModerationStatus(1L, "VISIBLE")).thenReturn(1L);

        museumService.deleteMyMuseumArtwork(1L, 2L, "user-1");

        verify(museum).updateVisibility(false);
    }
}
