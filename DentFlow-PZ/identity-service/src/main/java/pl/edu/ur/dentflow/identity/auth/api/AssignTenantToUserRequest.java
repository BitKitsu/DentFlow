package pl.edu.ur.dentflow.identity.auth.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AssignTenantToUserRequest(
        @NotNull @Min(1) Long userId,
        @NotNull @Min(1) Long tenantId
) {}
