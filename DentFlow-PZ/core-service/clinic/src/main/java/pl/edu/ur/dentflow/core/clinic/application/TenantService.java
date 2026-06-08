package pl.edu.ur.dentflow.core.clinic.application;

import pl.edu.ur.dentflow.core.clinic.api.*;
import pl.edu.ur.dentflow.core.clinic.domain.Location;
import pl.edu.ur.dentflow.core.clinic.domain.Tenant;
import pl.edu.ur.dentflow.core.clinic.infrastructure.LocationRepository;
import pl.edu.ur.dentflow.core.clinic.infrastructure.TenantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * Service managing clinics (tenants) in the DentFlow system.
 * Handles clinic registration, clinic data management
 * and location (branch) management.
 *
 * <p>Multi-tenant model: each clinic is a separate tenant, and all
 * business data (patients, appointments, schedules) is linked via tenantId.</p>
 *
 * <p>Clinic registration occurs after user registration (OWNER)
 * and creates a clinic with the first location.</p>
 *
 * @see pl.edu.ur.dentflow.core.clinic.domain.Tenant
 * @see pl.edu.ur.dentflow.core.clinic.domain.Location
 * @see pl.edu.ur.dentflow.core.clinic.infrastructure.TenantRepository
 */
@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final LocationRepository locationRepository;
    private final DataSource dataSource;

    public TenantService(TenantRepository tenantRepository,
                         LocationRepository locationRepository,
                         DataSource dataSource) {
        this.tenantRepository = tenantRepository;
        this.locationRepository = locationRepository;
        this.dataSource = dataSource;
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

    public List<TenantResponse> getAllTenants() {
        return tenantRepository.findAll().stream()
                .map(TenantResponse::from)
                .toList();
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

        // Each cleanup uses its own physical connection with autoCommit=true
        // because PostgreSQL aborts the entire transaction on any failed statement
        // (e.g. missing table). REQUIRES_NEW doesn't help because the outer
        // transaction's connection is reused. Raw JDBC bypasses this.
        // Order matters: delete deepest FK references first, then location/staff, then tenant.
        String[] cleanupSql = {
                "DELETE FROM appointment WHERE tenant_id = ?",
                "DELETE FROM work_schedule_slot WHERE tenant_id = ?",
                "DELETE FROM blocker WHERE tenant_id = ?",
                "DELETE FROM staff_working_hours WHERE staff_id IN (SELECT id FROM staff_member WHERE tenant_id = ?)",
                "DELETE FROM staff_room WHERE staff_id IN (SELECT id FROM staff_member WHERE tenant_id = ?)",
                "DELETE FROM room WHERE tenant_id = ?",
                "DELETE FROM staff_member WHERE tenant_id = ?",
                "DELETE FROM location WHERE tenant_id = ?"
        };
        for (String sql : cleanupSql) {
            safeDelete(sql, tenantId);
        }

        tenantRepository.delete(tenant);
    }

    private void safeDelete(String sql, Long tenantId) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(true);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, tenantId);
                ps.executeUpdate();
            }
        } catch (Exception ignored) {
        }
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
                        "Location does not exist"));
        if (!location.getTenant().getId().equals(tenantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Location does not belong to this clinic");
        }
        locationRepository.delete(location);
    }

    public LocationResponse getLocation(Long tenantId, Long locationId) {
        requireTenantExists(tenantId);
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Location does not exist"));
        if (!location.getTenant().getId().equals(tenantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Location does not belong to this clinic");
        }
        return LocationResponse.from(location);
    }

    @Transactional
    public LocationResponse updateLocation(Long tenantId, Long locationId, UpdateLocationRequest request) {
        requireTenantExists(tenantId);
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Location does not exist"));
        if (!location.getTenant().getId().equals(tenantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Location does not belong to this clinic");
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
