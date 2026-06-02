package com.dentflow.identity.auth.api;

import com.dentflow.identity.user.domain.Role;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AssignRoleRequest(
        @NotNull @Min(1) Long userId,
        @NotNull Role role
) {}
