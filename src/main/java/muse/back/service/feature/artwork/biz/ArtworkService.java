package muse.back.service.feature.artwork.biz;

import lombok.RequiredArgsConstructor;
import muse.back.service.database.pub.dto.ArtworkDetailResponse;
import muse.back.service.database.pub.entity.Artwork;
import muse.back.service.database.pub.entity.ArtworkAsset;
import muse.back.service.database.pub.repository.ArtworkAssetRepository;
import muse.back.service.database.pub.repository.ArtworkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtworkService {

    private final ArtworkRepository artworkRepository;
    private final ArtworkAssetRepository artworkAssetRepository;

    public ArtworkDetailResponse getArtworkDetail(Long id) {
        Artwork artwork = artworkRepository.findById(id)
                .orElseThrow(() -> new GeneralException(
                        Code.NOT_FOUND,
                        String.format("Artwork not found with id: '%s'", id)
                ));
        ArtworkAsset currentAsset = artworkAssetRepository.findById(id).orElse(null);
        List<Artwork> relatedArtworkEntities = artworkRepository
                .findTop3ByCategoryKeyAndArtworkIdNotOrderByArtworkIdDesc(
                        artwork.getCategoryKey(),
                        artwork.getArtworkId()
                );
        Map<Long, ArtworkAsset> relatedAssetMap = artworkAssetRepository
                .findAllById(relatedArtworkEntities.stream().map(Artwork::getArtworkId).toList())
                .stream()
                .collect(Collectors.toMap(ArtworkAsset::getArtworkId, Function.identity()));
        List<ArtworkDetailResponse.RelatedWork> relatedWorks = relatedArtworkEntities.stream()
                .map(relatedArtwork -> toRelatedWork(
                        relatedArtwork,
                        relatedAssetMap.get(relatedArtwork.getArtworkId())
                ))
                .toList();

        return new ArtworkDetailResponse(
                artwork.getArtworkId(),
                artwork.getTitle(),
                artwork.getArtist(),
                resolveCategoryLabel(artwork),
                artwork.getDescription(),
                currentAsset == null ? null : currentAsset.getImageUrl(),
                artwork.getColorFrom(),
                artwork.getColorTo(),
                new ArtworkDetailResponse.Exif(
                        artwork.getCamera(),
                        artwork.getLens(),
                        artwork.getFocalLength(),
                        artwork.getAperture(),
                        artwork.getShutterSpeed(),
                        artwork.getIso()
                ),
                relatedWorks
        );
    }

    private ArtworkDetailResponse.RelatedWork toRelatedWork(Artwork artwork, ArtworkAsset asset) {
        return new ArtworkDetailResponse.RelatedWork(
                artwork.getArtworkId(),
                artwork.getTitle(),
                artwork.getArtist(),
                asset == null ? null : asset.getImageUrl(),
                artwork.getColorFrom(),
                artwork.getColorTo()
        );
    }

    private String resolveCategoryLabel(Artwork artwork) {
        if (artwork.getCategoryLabel() != null && !artwork.getCategoryLabel().isBlank()) {
            return artwork.getCategoryLabel();
        }
        return artwork.getCategoryKey();
    }
}
