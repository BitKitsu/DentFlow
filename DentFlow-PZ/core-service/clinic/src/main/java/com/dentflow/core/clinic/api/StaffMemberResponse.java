package com.dentflow.core.clinic.api;

import com.dentflow.core.clinic.domain.StaffMember;

public record StaffMemberResponse(
        Long id,
        Long tenantId,
        Long userId,
        String displayName,
        String profession
) {
    public static StaffMemberResponse from(StaffMember staffMember) {
        return new StaffMemberResponse(
                staffMember.getId(),
                staffMember.getTenant().getId(),
                staffMember.getUserId(),
                staffMember.getDisplayName(),
                staffMember.getProfession()
        );
    }
}
