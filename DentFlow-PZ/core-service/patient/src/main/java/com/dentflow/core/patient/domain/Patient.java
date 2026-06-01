package com.dentflow.core.patient.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "patient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "pesel", length = 11)
    private String pesel;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "address_street", length = 100)
    private String addressStreet;

    @Column(name = "address_city", length = 100)
    private String addressCity;

    @Column(name = "address_zip", length = 20)
    private String addressZip;

    @Column(name = "address_country", length = 100)
    private String addressCountry;
}
