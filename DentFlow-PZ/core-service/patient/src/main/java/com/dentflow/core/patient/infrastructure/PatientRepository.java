package com.dentflow.core.patient.infrastructure;

import com.dentflow.core.patient.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    
    List<Patient> findByTenantId(Long tenantId);
    
    Optional<Patient> findByIdAndTenantId(Long id, Long tenantId);
    
    @Query("SELECT p FROM Patient p WHERE p.tenantId = :tenantId AND " +
           "(LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "p.phone LIKE CONCAT('%', :searchTerm, '%'))")
    List<Patient> searchPatients(@Param("tenantId") Long tenantId, @Param("searchTerm") String searchTerm);
}
