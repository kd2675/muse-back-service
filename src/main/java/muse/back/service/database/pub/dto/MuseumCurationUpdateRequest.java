package muse.back.service.database.pub.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record MuseumCurationUpdateRequest(
        String publishStatus,
        Long coverArtworkId,
        LocalDateTime openingAt,
        @Size(max = 2000) String curatorNote,
        String layoutPreset,
        String lightingPreset
) {}
