package muse.back.service.database.pub.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentOrderCreateRequest(@NotNull @Positive Long contestId) {}
