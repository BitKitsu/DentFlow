package pl.edu.ur.dentflow.core.clinic.application;

import pl.edu.ur.dentflow.core.clinic.api.CreateStaffMemberRequest;
import pl.edu.ur.dentflow.core.clinic.api.StaffMemberResponse;
import pl.edu.ur.dentflow.core.clinic.api.UpdateStaffMemberRequest;
import pl.edu.ur.dentflow.core.clinic.domain.StaffMember;
import pl.edu.ur.dentflow.core.clinic.domain.Tenant;
import pl.edu.ur.dentflow.core.clinic.infrastructure.StaffMemberRepository;
import pl.edu.ur.dentflow.core.clinic.infrastructure.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffMemberServiceTest {

    @Mock
    private StaffMemberRepository staffMemberRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private StaffMemberService staffMemberService;

    private Tenant tenant;
    private StaffMember staffMember;

    @BeforeEach
    void setUp() {
        tenant = Tenant.builder().id(1L).name("Test Clinic").build();
        staffMember = StaffMember.builder()
                .id(10L)
                .tenant(tenant)
                .userId(100L)
                .firstName("Dr.")
                .lastName("Smith")
                .profession("Dentist")
                .bio("I am a dentist")
                .build();
    }

    @Test
    void shouldReturnStaffMembers() {
        when(tenantRepository.existsById(1L)).thenReturn(true);
        when(staffMemberRepository.findByTenantId(1L)).thenReturn(List.of(staffMember));

        List<StaffMemberResponse> responses = staffMemberService.getStaffMembers(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).firstName()).isEqualTo("Dr.");
        assertThat(responses.get(0).lastName()).isEqualTo("Smith");
        assertThat(responses.get(0).profession()).isEqualTo("Dentist");
    }

    @Test
    void shouldAddStaffMember() {
        CreateStaffMemberRequest req = new CreateStaffMemberRequest(200L, "Dr.", "Jones", "Assistant", "I am an assistant", null, null, null, java.time.LocalTime.of(8,0), java.time.LocalTime.of(16,0));
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        
        StaffMember newStaff = StaffMember.builder()
            .id(11L).tenant(tenant).userId(req.userId())
            .firstName(req.firstName()).lastName(req.lastName()).profession(req.profession()).bio(req.bio()).build();

        when(staffMemberRepository.save(any(StaffMember.class))).thenReturn(newStaff);

        StaffMemberResponse response = staffMemberService.addStaffMember(1L, req);

        assertThat(response.firstName()).isEqualTo("Dr.");
        assertThat(response.lastName()).isEqualTo("Jones");
        verify(staffMemberRepository).save(any(StaffMember.class));
    }

    @Test
    void shouldUpdateStaffMember() {
        UpdateStaffMemberRequest req = new UpdateStaffMemberRequest(100L, "Dr.", "Smith Updated", "Lead Dentist", "Updated bio", java.time.LocalTime.of(8,0), java.time.LocalTime.of(16,0));
        when(staffMemberRepository.findByIdAndTenantId(10L, 1L)).thenReturn(Optional.of(staffMember));
        when(staffMemberRepository.save(any(StaffMember.class))).thenReturn(staffMember);

        StaffMemberResponse response = staffMemberService.updateStaffMember(1L, 10L, req);

        assertThat(response.lastName()).isEqualTo("Smith Updated");
        assertThat(response.profession()).isEqualTo("Lead Dentist");
        verify(staffMemberRepository).save(staffMember);
    }

    @Test
    void shouldDeleteStaffMember() {
        when(staffMemberRepository.findByIdAndTenantId(10L, 1L)).thenReturn(Optional.of(staffMember));

        staffMemberService.deleteStaffMember(1L, 10L);

        verify(staffMemberRepository).delete(staffMember);
    }

    @Test
    void shouldThrowWhenTenantNotFound() {
        when(tenantRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> staffMemberService.getStaffMembers(99L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void shouldThrowWhenAddingStaffToNonExistentTenant() {
        when(tenantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> staffMemberService.addStaffMember(99L, mock(CreateStaffMemberRequest.class)))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void shouldThrowWhenStaffMemberNotFound() {
        when(staffMemberRepository.findByIdAndTenantId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> staffMemberService.getStaffMember(1L, 99L))
                .isInstanceOf(ResponseStatusException.class);
        
        assertThatThrownBy(() -> staffMemberService.updateStaffMember(1L, 99L, mock(UpdateStaffMemberRequest.class)))
                .isInstanceOf(ResponseStatusException.class);

        assertThatThrownBy(() -> staffMemberService.deleteStaffMember(1L, 99L))
                .isInstanceOf(ResponseStatusException.class);
    }
}
