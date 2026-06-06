package pl.edu.ur.dentflow.core.clinic.api;

import pl.edu.ur.dentflow.core.clinic.domain.Location;

public record LocationResponse(
        Long id,
        Long tenantId,
        String name,
        String addressStreet,
        String addressCity,
        String addressZip,
        String addressCountry
) {
    public static LocationResponse from(Location location) {
        return new LocationResponse(
                location.getId(),
                location.getTenant().getId(),
                location.getName(),
                location.getAddressStreet(),
                location.getAddressCity(),
                location.getAddressZip(),
                location.getAddressCountry()
        );
    }
}
