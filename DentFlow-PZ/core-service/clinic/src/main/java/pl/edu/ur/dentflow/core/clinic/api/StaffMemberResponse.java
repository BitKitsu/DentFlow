package pl.edu.ur.dentflow.core.clinic.api;

import pl.edu.ur.dentflow.core.clinic.domain.StaffMember;
import java.time.LocalTime;

public record StaffMemberResponse(
        Long id,
        Long tenantId,
        Long userId,
        String firstName,
        String lastName,
        String profession,
        String bio,
        String avatarUrl,
        String phone,
        String email,
        LocalTime workingHoursStart,
        LocalTime workingHoursEnd
) {
    public static StaffMemberResponse from(StaffMember staffMember) {
        return new StaffMemberResponse(
                staffMember.getId(),
                staffMember.getTenant().getId(),
                staffMember.getUserId(),
                staffMember.getFirstName(),
                staffMember.getLastName(),
                staffMember.getProfession(),
                staffMember.getBio(),
                staffMember.getAvatarUrl(),
                staffMember.getPhone(),
                staffMember.getEmail(),
                staffMember.getWorkingHoursStart(),
                staffMember.getWorkingHoursEnd()
        );
    }
}
