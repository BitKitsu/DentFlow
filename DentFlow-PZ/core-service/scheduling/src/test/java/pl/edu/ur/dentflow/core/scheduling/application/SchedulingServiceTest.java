package pl.edu.ur.dentflow.core.scheduling.application;

import pl.edu.ur.dentflow.core.scheduling.api.CreateBlockerRequest;
import pl.edu.ur.dentflow.core.scheduling.api.BlockerResponse;
import pl.edu.ur.dentflow.core.scheduling.domain.Blocker;
import pl.edu.ur.dentflow.core.scheduling.infrastructure.BlockerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulingServiceTest {

    @Mock
    private BlockerRepository blockerRepository;

    @InjectMocks
    private SchedulingService schedulingService;

    private final OffsetDateTime now = OffsetDateTime.now();
    private final OffsetDateTime later = now.plusHours(2);

    private Blocker blocker;

    @BeforeEach
    void setUp() {
        blocker = Blocker.builder()
                .id(2L).tenantId(10L).staffId(20L)
                .startAt(now).endAt(later).reason("Urlop")
                .build();
    }

    @Test
    void shouldReturnBlockers() {
        when(blockerRepository.findByTenantId(10L)).thenReturn(List.of(blocker));

        List<BlockerResponse> result = schedulingService.getBlockers(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).reason()).isEqualTo("Urlop");
    }

    @Test
    void shouldAddBlocker() {
        CreateBlockerRequest req = new CreateBlockerRequest(20L, null, now, later, "Urlop");
        when(blockerRepository.save(any())).thenAnswer(inv -> {
            Blocker b = inv.getArgument(0);
            b.setId(55L);
            return b;
        });

        BlockerResponse response = schedulingService.addBlocker(10L, req);

        assertThat(response.id()).isEqualTo(55L);
    }

    @Test
    void shouldDeleteBlocker() {
        when(blockerRepository.findById(2L)).thenReturn(Optional.of(blocker));

        schedulingService.deleteBlocker(10L, 2L);

        verify(blockerRepository).delete(blocker);
    }
}
