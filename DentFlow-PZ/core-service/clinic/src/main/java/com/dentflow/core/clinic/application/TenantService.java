package com.dentflow.core.clinic.application;

import com.dentflow.core.clinic.api.*;
import com.dentflow.core.clinic.domain.Location;
import com.dentflow.core.clinic.domain.Tenant;
import com.dentflow.core.clinic.infrastructure.LocationRepository;
import com.dentflow.core.clinic.infrastructure.TenantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final LocationRepository locationRepository;

    public TenantService(TenantRepository tenantRepository,
                         LocationRepository locationRepository) {
        this.tenantRepository = tenantRepository;
        this.locationRepository = locationRepository;
    }

    /**
     * Rejestracja gabinetu (tenant) wraz z pierwszą lokalizacją.
     * Wywoływana synchronicznie po rejestracji użytkownika OWNER w identity-service.
     * Zwraca TenantResponse - id gabinetu służy jako tenantId w JWT.
     */
    @Transactional
    public TenantResponse registerTenant(RegisterTenantRequest request) {
        Tenant tenant = Tenant.builder()
                .name(request.name())
                .build();

        Location firstLocation = Location.builder()
                .tenant(tenant)
                .name(request.locationName())
                .addressStreet(request.addressStreet())
                .addressCity(request.addressCity())
                .addressZip(request.addressZip())
                .addressCountry(request.addressCountry())
                .build();

        tenant.getLocations().add(firstLocation);
        Tenant saved = tenantRepository.save(tenant);
        return TenantResponse.from(saved);
    }

    public TenantResponse getTenant(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Gabinet nie istnieje"));
        return TenantResponse.from(tenant);
    }

    @Transactional
    public TenantResponse updateTenant(Long tenantId, UpdateTenantRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Gabinet nie istnieje"));
        tenant.setName(request.name());
        return TenantResponse.from(tenantRepository.save(tenant));
    }

    // ----- Zarządzanie lokalizacjami (stub) -----

    public List<LocationResponse> getLocations(Long tenantId) {
        requireTenantExists(tenantId);
        return locationRepository.findByTenantId(tenantId).stream()
                .map(LocationResponse::from)
                .toList();
    }

    @Transactional
    public LocationResponse addLocation(Long tenantId, AddLocationRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Gabinet nie istnieje"));

        Location location = Location.builder()
                .tenant(tenant)
                .name(request.name())
                .addressStreet(request.addressStreet())
                .addressCity(request.addressCity())
                .addressZip(request.addressZip())
                .addressCountry(request.addressCountry())
                .build();

        return LocationResponse.from(locationRepository.save(location));
    }

    @Transactional
    public void deleteLocation(Long tenantId, Long locationId) {
        requireTenantExists(tenantId);
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Lokalizacja nie istnieje"));
        if (!location.getTenant().getId().equals(tenantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Lokalizacja nie należy do tego gabinetu");
        }
        locationRepository.delete(location);
    }

    public LocationResponse getLocation(Long tenantId, Long locationId) {
        requireTenantExists(tenantId);
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Lokalizacja nie istnieje"));
        if (!location.getTenant().getId().equals(tenantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Lokalizacja nie należy do tego gabinetu");
        }
        return LocationResponse.from(location);
    }

    @Transactional
    public LocationResponse updateLocation(Long tenantId, Long locationId, UpdateLocationRequest request) {
        requireTenantExists(tenantId);
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Lokalizacja nie istnieje"));
        if (!location.getTenant().getId().equals(tenantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Lokalizacja nie należy do tego gabinetu");
        }

        location.setName(request.name());
        location.setAddressStreet(request.addressStreet());
        location.setAddressCity(request.addressCity());
        location.setAddressZip(request.addressZip());
        location.setAddressCountry(request.addressCountry());

        return LocationResponse.from(locationRepository.save(location));
    }

    private void requireTenantExists(Long tenantId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Gabinet nie istnieje");
        }
    }
}
