package com.dentflow.core.clinic.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "staff_member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "profession", nullable = false, length = 20)
    private String profession;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", length = 1000)
    private String avatarUrl;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;
}
