package muse.back.service.database.pub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PaymentCancelRequest(@NotBlank @Size(max = 200) String reason) {}
