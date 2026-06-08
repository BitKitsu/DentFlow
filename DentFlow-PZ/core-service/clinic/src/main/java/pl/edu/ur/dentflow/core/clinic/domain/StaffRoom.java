package pl.edu.ur.dentflow.core.clinic.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "staff_room",
        uniqueConstraints = @UniqueConstraint(columnNames = {"staff_id", "room_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private StaffMember staffMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
}
