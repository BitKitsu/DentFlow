package com.dentflow.core.clinic.api;

import com.dentflow.core.clinic.domain.Tenant;

import java.util.List;
package com.dentflow.identity.auth.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AssignTenantRequest(
        @NotNull @Min(1) Long tenantId
) {}

public record TenantResponse(
        Long id,
        String name,
        String status,
        List<LocationResponse> locations
) {
    public static TenantResponse from(Tenant tenant) {
        List<LocationResponse> locs = tenant.getLocations().stream()
                .map(LocationResponse::from)
                .toList();
        return new TenantResponse(tenant.getId(), tenant.getName(), tenant.getStatus(), locs);
    }
}
