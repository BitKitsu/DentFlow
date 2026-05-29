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
     * Tenant registration with first location
     * Ran synchronious after OWNER user registration in identity-service
     * Returns TenantResponse
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
        if (request.logoUrl() != null) tenant.setLogoUrl(request.logoUrl());

        // Update first location if provided
        if (request.locationName() != null || request.addressStreet() != null) {
            List<Location> locations = locationRepository.findByTenantId(tenantId);
            if (!locations.isEmpty()) {
                Location loc = locations.get(0);
                if (request.locationName() != null) loc.setName(request.locationName());
                if (request.addressStreet() != null) loc.setAddressStreet(request.addressStreet());
                if (request.addressCity() != null) loc.setAddressCity(request.addressCity());
                if (request.addressZip() != null) loc.setAddressZip(request.addressZip());
                if (request.addressCountry() != null) loc.setAddressCountry(request.addressCountry());
                locationRepository.save(loc);
            }
        }

        return TenantResponse.from(tenantRepository.save(tenant));
    }

    @Transactional
    public void deleteTenant(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Gabinet nie istnieje"));
        tenantRepository.delete(tenant);
    }

    // Managing locations

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
