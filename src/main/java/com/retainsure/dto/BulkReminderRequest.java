package com.retainsure.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BulkReminderRequest(
        @Min(0) int riskThreshold,
        @NotBlank String dateSent,
        @NotNull String mode,
        @NotBlank String triggerMsg
) {}