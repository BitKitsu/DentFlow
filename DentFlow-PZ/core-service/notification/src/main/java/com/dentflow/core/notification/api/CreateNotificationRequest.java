package com.dentflow.core.notification.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateNotificationRequest(
        @NotNull Long userId,
        @NotBlank String type,
        @NotBlank String message
) {}
