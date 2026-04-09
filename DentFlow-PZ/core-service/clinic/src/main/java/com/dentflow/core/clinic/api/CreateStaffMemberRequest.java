package com.dentflow.core.clinic.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateStaffMemberRequest(
        Long userId, // Opcjonalne przypisanie do konta użytkownika

        @NotBlank @Size(max = 100)
        String displayName,

        @NotBlank @Size(max = 20)
        String profession
) {}
