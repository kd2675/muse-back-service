package muse.back.service.database.pub.dto;

import java.util.List;

public record AdminGalleryHighlightUpdateRequest(
        List<Long> artworkIds
) {
}
