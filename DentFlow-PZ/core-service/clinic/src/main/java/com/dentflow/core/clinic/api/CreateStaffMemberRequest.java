package com.dentflow.core.clinic.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record CreateStaffMemberRequest(
        Long userId,

        @NotBlank @Size(max = 100)
        String firstName,

        @NotBlank @Size(max = 100)
        String lastName,

        @NotBlank @Size(max = 20)
        String profession,

        String bio,

        String avatarUrl,

        String phone,

        String email,

        LocalTime workingHoursStart,

        LocalTime workingHoursEnd
) {}
