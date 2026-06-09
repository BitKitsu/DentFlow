package pl.edu.ur.dentflow.core.clinic.api;

import pl.edu.ur.dentflow.core.clinic.domain.Tenant;

import java.util.List;

public record TenantResponse(
        Long id,
        String name,
        String status,
        String logoUrl,
        List<LocationResponse> locations
) {
    public static TenantResponse from(Tenant tenant) {
        List<LocationResponse> locs = tenant.getLocations().stream()
                .map(LocationResponse::from)
                .toList();
        return new TenantResponse(tenant.getId(), tenant.getName(), tenant.getStatus(), tenant.getLogoUrl(), locs);
    }
}
