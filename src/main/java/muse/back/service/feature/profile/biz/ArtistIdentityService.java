package muse.back.service.feature.profile.biz;

import lombok.RequiredArgsConstructor;
import muse.back.service.database.pub.entity.ProfileArtist;
import muse.back.service.database.pub.repository.ProfileArtistRepository;
import org.springframework.stereotype.Service;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

@Service
@RequiredArgsConstructor
public class ArtistIdentityService {
    private final ProfileArtistRepository profileArtistRepository;

    public ProfileArtist requireByUserKey(String userKey) {
        if (userKey == null || userKey.isBlank()) {
            throw new GeneralException(Code.UNAUTHORIZED, "Login required");
        }
        return profileArtistRepository.findByUserKey(userKey)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Artist profile not found"));
    }

    public ProfileArtist requireById(Long artistId) {
        return profileArtistRepository.findById(artistId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Artist not found"));
    }
}
