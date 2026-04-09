package com.dentflow.core.clinic.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateStaffMemberRequest(
        Long userId,

        @NotBlank @Size(max = 100)
        String displayName,

        @NotBlank @Size(max = 20)
        String profession
) {}
