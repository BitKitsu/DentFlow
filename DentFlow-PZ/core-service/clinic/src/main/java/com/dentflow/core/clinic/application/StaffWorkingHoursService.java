package com.dentflow.core.clinic.application;

import com.dentflow.core.clinic.api.StaffWorkingHoursDTO;
import com.dentflow.core.clinic.api.UpdateWorkingHoursRequest;
import com.dentflow.core.clinic.api.WorkingHoursEntry;
import com.dentflow.core.clinic.domain.StaffMember;
import com.dentflow.core.clinic.domain.StaffWorkingHours;
import com.dentflow.core.clinic.infrastructure.StaffMemberRepository;
import com.dentflow.core.clinic.infrastructure.StaffWorkingHoursRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class StaffWorkingHoursService {

    private final StaffWorkingHoursRepository repository;
    private final StaffMemberRepository staffMemberRepository;

    public StaffWorkingHoursService(StaffWorkingHoursRepository repository,
                                     StaffMemberRepository staffMemberRepository) {
        this.repository = repository;
        this.staffMemberRepository = staffMemberRepository;
    }

    @Transactional(readOnly = true)
    public List<StaffWorkingHoursDTO> getWorkingHours(Long tenantId, Long staffId) {
        StaffMember staff = requireStaffMember(tenantId, staffId);
        return repository.findByStaffMemberIdOrderByDayOfWeekAsc(staff.getId()).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public void updateWorkingHours(Long tenantId, Long staffId, UpdateWorkingHoursRequest request) {
        StaffMember staff = requireStaffMember(tenantId, staffId);
        repository.deleteByStaffMemberId(staff.getId());

        for (WorkingHoursEntry entry : request.schedule()) {
            if (entry.dayOfWeek() < 1 || entry.dayOfWeek() > 7) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "dayOfWeek musi być w zakresie 1-7");
            }
            if (!entry.endTime().isAfter(entry.startTime())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "endTime musi być po startTime dla dnia " + entry.dayOfWeek());
            }
            StaffWorkingHours wh = StaffWorkingHours.builder()
                    .staffMember(staff)
                    .dayOfWeek(entry.dayOfWeek())
                    .startTime(entry.startTime())
                    .endTime(entry.endTime())
                    .build();
            repository.save(wh);
        }
    }

    private StaffMember requireStaffMember(Long tenantId, Long staffId) {
        return staffMemberRepository.findByIdAndTenantId(staffId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Pracownik nie istnieje w tym gabinecie"));
    }

    private StaffWorkingHoursDTO toDTO(StaffWorkingHours wh) {
        return new StaffWorkingHoursDTO(
                wh.getId(),
                wh.getStaffMember().getId(),
                wh.getDayOfWeek(),
                wh.getStartTime(),
                wh.getEndTime()
        );
    }
}
