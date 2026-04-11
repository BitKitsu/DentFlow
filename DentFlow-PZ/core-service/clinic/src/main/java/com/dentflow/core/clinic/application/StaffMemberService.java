package com.dentflow.core.clinic.application;

import com.dentflow.core.clinic.api.CreateStaffMemberRequest;
import com.dentflow.core.clinic.api.StaffMemberResponse;
import com.dentflow.core.clinic.api.UpdateStaffMemberRequest;
import com.dentflow.core.clinic.domain.StaffMember;
import com.dentflow.core.clinic.domain.Tenant;
import com.dentflow.core.clinic.infrastructure.StaffMemberRepository;
import com.dentflow.core.clinic.infrastructure.TenantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class StaffMemberService {

    private final StaffMemberRepository staffMemberRepository;
    private final TenantRepository tenantRepository;

    public StaffMemberService(StaffMemberRepository staffMemberRepository, TenantRepository tenantRepository) {
        this.staffMemberRepository = staffMemberRepository;
        this.tenantRepository = tenantRepository;
    }

    public List<StaffMemberResponse> getStaffMembers(Long tenantId) {
        requireTenantExists(tenantId);
        return staffMemberRepository.findByTenantId(tenantId).stream()
                .map(StaffMemberResponse::from)
                .toList();
    }

    public StaffMemberResponse getStaffMember(Long tenantId, Long staffId) {
        StaffMember staff = staffMemberRepository.findByIdAndTenantId(staffId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pracownik nie istnieje w tym gabinecie"));
        return StaffMemberResponse.from(staff);
    }

    @Transactional
    public StaffMemberResponse addStaffMember(Long tenantId, CreateStaffMemberRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gabinet nie istnieje"));

        StaffMember staff = StaffMember.builder()
                .tenant(tenant)
                .userId(request.userId())
                .displayName(request.displayName())
                .profession(request.profession())
                .build();

        return StaffMemberResponse.from(staffMemberRepository.save(staff));
    }

    @Transactional
    public StaffMemberResponse updateStaffMember(Long tenantId, Long staffId, UpdateStaffMemberRequest request) {
        StaffMember staff = staffMemberRepository.findByIdAndTenantId(staffId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pracownik nie istnieje w tym gabinecie"));

        staff.setDisplayName(request.displayName());
        staff.setProfession(request.profession());
        if (request.userId() != null) {
            staff.setUserId(request.userId());
        }

        return StaffMemberResponse.from(staffMemberRepository.save(staff));
    }

    @Transactional
    public void deleteStaffMember(Long tenantId, Long staffId) {
        StaffMember staff = staffMemberRepository.findByIdAndTenantId(staffId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pracownik nie istnieje w tym gabinecie"));
        staffMemberRepository.delete(staff);
    }

    private void requireTenantExists(Long tenantId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Gabinet nie istnieje");
        }
    }
}
