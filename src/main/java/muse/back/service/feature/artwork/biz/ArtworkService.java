package muse.back.service.feature.artwork.biz;

import lombok.RequiredArgsConstructor;
import muse.back.service.database.pub.dto.ArtworkDetailResponse;
import muse.back.service.database.pub.entity.Artwork;
import muse.back.service.database.pub.repository.ArtworkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtworkService {

    private final ArtworkRepository artworkRepository;

    public ArtworkDetailResponse getArtworkDetail(Long id) {
        Artwork artwork = artworkRepository.findById(id)
                .orElseThrow(() -> new GeneralException(
                        Code.NOT_FOUND,
                        String.format("Artwork not found with id: '%s'", id)
                ));

        return new ArtworkDetailResponse(
                artwork.getArtworkId(),
                artwork.getTitle(),
                artwork.getArtist(),
                resolveCategoryLabel(artwork),
                artwork.getDescription(),
                artwork.getColorFrom(),
                artwork.getColorTo(),
                new ArtworkDetailResponse.Exif(
                        artwork.getCamera(),
                        artwork.getLens(),
                        artwork.getFocalLength(),
                        artwork.getAperture(),
                        artwork.getShutterSpeed(),
                        artwork.getIso()
                )
        );
    }

    private String resolveCategoryLabel(Artwork artwork) {
        if (artwork.getCategoryLabel() != null && !artwork.getCategoryLabel().isBlank()) {
            return artwork.getCategoryLabel();
        }
        return artwork.getCategoryKey();
    }
}
