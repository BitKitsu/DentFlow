package pl.edu.ur.dentflow.core.patient.api;

import pl.edu.ur.dentflow.core.patient.domain.Patient;
import java.time.LocalDate;

public record PatientResponse(
        Long id,
        Long tenantId,
        Long userId,
        String firstName,
        String lastName,
        String phone,
        String email,
        String notes,
        LocalDate dateOfBirth,
        String pesel,
        String gender,
        String addressStreet,
        String addressCity,
        String addressZip,
        String addressCountry,
        String avatarUrl
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
                patient.getNotes(),
                patient.getDateOfBirth(),
                patient.getPesel(),
                patient.getGender(),
                patient.getAddressStreet(),
                patient.getAddressCity(),
                patient.getAddressZip(),
                patient.getAddressCountry(),
                patient.getAvatarUrl()
        );
    }
}
