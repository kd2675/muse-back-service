package muse.back.service.feature.profile.biz;

import muse.back.service.database.pub.dto.ProfileSummaryResponse;
import muse.back.service.database.pub.repository.ProfileArtistRepository;
import muse.back.service.database.pub.repository.ProfileAwardRepository;
import muse.back.service.database.pub.repository.ProfilePortfolioRepository;
import muse.back.service.database.pub.repository.ProfileStatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileArtistRepository profileArtistRepository;
    @Mock
    private ProfileStatRepository profileStatRepository;
    @Mock
    private ProfilePortfolioRepository profilePortfolioRepository;
    @Mock
    private ProfileAwardRepository profileAwardRepository;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void getProfileSummary_returnsFallbackProfile_whenArtistAndStatMissing() {
        String userKey = "usr-test-42";
        Long fallbackArtistId = 0L;

        when(profileArtistRepository.findByUserKey(userKey)).thenReturn(Optional.empty());
        when(profileStatRepository.findByArtistId(fallbackArtistId)).thenReturn(Optional.empty());
        when(profilePortfolioRepository.findByArtistIdOrderByPortfolioIdAsc(fallbackArtistId)).thenReturn(List.of());
        when(profileAwardRepository.findByArtistIdOrderByAwardIdAsc(fallbackArtistId)).thenReturn(List.of());

        ProfileSummaryResponse response = profileService.getProfileSummary(userKey);

        assertThat(response.artist().name()).isEqualTo("Artist usr-test-42");
        assertThat(response.artist().tagline()).isEqualTo("프로필 초기화 전");
        assertThat(response.stats().totalWorks()).isEqualTo(0);
        assertThat(response.stats().totalAwards()).isEqualTo(0);
        assertThat(response.stats().totalEarnings()).isEqualTo(0);
        assertThat(response.stats().followers()).isEqualTo(0);
        assertThat(response.portfolio()).isEmpty();
        assertThat(response.awards()).isEmpty();
    }
}
