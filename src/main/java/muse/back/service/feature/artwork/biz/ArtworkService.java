package muse.back.service.feature.artwork.biz;

import muse.back.service.common.exception.MuseException;
import muse.back.service.database.pub.dto.ArtworkDetailResponse;
import org.springframework.stereotype.Service;

@Service
public class ArtworkService {

    public ArtworkDetailResponse getArtworkDetail(Long id) {
        return switch (id.intValue()) {
            case 201 -> buildArtwork(
                    201L,
                    "Stillness of Air",
                    "Jiyoon Park",
                    "Fine Art",
                    "차분한 빛과 질감을 통해 공기의 움직임을 시각화한 작품.",
                    "#1B1B1B",
                    "#C7B89A",
                    new ArtworkDetailResponse.Exif(
                            "Sony A7R V",
                            "FE 50mm F1.2 GM",
                            "50mm",
                            "f/2.0",
                            "1/160s",
                            "ISO 200"
                    )
            );
            case 202 -> buildArtwork(
                    202L,
                    "Golden Horizon",
                    "Noah Kim",
                    "Landscape",
                    "일출 직전의 황금빛 지평선을 포착한 장면.",
                    "#4B3B2F",
                    "#E2C08D",
                    new ArtworkDetailResponse.Exif(
                            "Canon EOS R5",
                            "RF 24-70mm F2.8",
                            "35mm",
                            "f/5.6",
                            "1/250s",
                            "ISO 100"
                    )
            );
            case 203 -> buildArtwork(
                    203L,
                    "City Pulse",
                    "Arin Lee",
                    "Urban",
                    "도시의 리듬과 흐름을 추상적 실루엣으로 담아낸 작품.",
                    "#1E2A35",
                    "#6B7C93",
                    new ArtworkDetailResponse.Exif(
                            "Nikon Z8",
                            "Z 24-120mm F4",
                            "70mm",
                            "f/4.5",
                            "1/80s",
                            "ISO 400"
                    )
            );
            default -> throw new MuseException.ResourceNotFoundException(
                    "Artwork",
                    "id",
                    id
            );
        };
    }

    private ArtworkDetailResponse buildArtwork(
            Long id,
            String title,
            String artist,
            String category,
            String description,
            String colorFrom,
            String colorTo,
            ArtworkDetailResponse.Exif exif
    ) {
        return new ArtworkDetailResponse(
                id,
                title,
                artist,
                category,
                description,
                colorFrom,
                colorTo,
                exif
        );
    }
}
