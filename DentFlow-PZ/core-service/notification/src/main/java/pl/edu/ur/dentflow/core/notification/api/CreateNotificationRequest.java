package pl.edu.ur.dentflow.core.notification.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateNotificationRequest(
        @NotNull Long userId,
        @NotBlank @Size(max = 50) String type,
        @NotBlank @Size(max = 5000) String message
) {}
