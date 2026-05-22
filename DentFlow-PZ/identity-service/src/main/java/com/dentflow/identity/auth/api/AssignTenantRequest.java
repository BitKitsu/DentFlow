package com.dentflow.identity.auth.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AssignTenantRequest(
        @NotNull @Min(1) Long tenantId
) {}
