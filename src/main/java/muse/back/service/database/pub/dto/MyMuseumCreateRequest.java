package muse.back.service.database.pub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MyMuseumCreateRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 1000) String description
) {
}
