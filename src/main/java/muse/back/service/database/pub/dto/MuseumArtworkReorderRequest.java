package muse.back.service.database.pub.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record MuseumArtworkReorderRequest(@NotEmpty List<@Valid Item> items) {
    public record Item(Long museumArtworkId, @Min(0) int sortOrder) {}
}
