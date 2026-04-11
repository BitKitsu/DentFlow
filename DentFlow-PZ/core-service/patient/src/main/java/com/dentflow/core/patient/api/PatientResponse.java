package com.dentflow.core.patient.api;

import com.dentflow.core.patient.domain.Patient;

public record PatientResponse(
        Long id,
        Long tenantId,
        Long userId,
        String firstName,
        String lastName,
        String phone,
        String email,
        String notes
) {
    public static PatientResponse from(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getTenantId(),
                patient.getUserId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getPhone(),
                patient.getEmail(),
                patient.getNotes()
        );
    }
}
