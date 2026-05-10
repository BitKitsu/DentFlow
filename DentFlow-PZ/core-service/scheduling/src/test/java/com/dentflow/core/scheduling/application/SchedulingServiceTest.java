package com.dentflow.core.scheduling.application;

import com.dentflow.core.scheduling.api.CreateBlockerRequest;
import com.dentflow.core.scheduling.api.CreateWorkScheduleSlotRequest;
import com.dentflow.core.scheduling.api.BlockerResponse;
import com.dentflow.core.scheduling.api.WorkScheduleSlotResponse;
import com.dentflow.core.scheduling.domain.Blocker;
import com.dentflow.core.scheduling.domain.WorkScheduleSlot;
import com.dentflow.core.scheduling.infrastructure.BlockerRepository;
import com.dentflow.core.scheduling.infrastructure.WorkScheduleSlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulingServiceTest {

    @Mock
    private WorkScheduleSlotRepository slotRepository;

    @Mock
    private BlockerRepository blockerRepository;

    @InjectMocks
    private SchedulingService schedulingService;

    private final OffsetDateTime now = OffsetDateTime.now();
    private final OffsetDateTime later = now.plusHours(2);

    private WorkScheduleSlot slot;
    private Blocker blocker;

    @BeforeEach
    void setUp() {
        slot = WorkScheduleSlot.builder()
                .id(1L).tenantId(10L).staffId(20L)
                .locationId(30L).startAt(now).endAt(later)
                .build();

        blocker = Blocker.builder()
                .id(2L).tenantId(10L).staffId(20L)
                .startAt(now).endAt(later).reason("Urlop")
                .build();
    }

    @Test
    void shouldReturnAllSlotsWhenNoDateRange() {
        when(slotRepository.findByTenantId(10L)).thenReturn(List.of(slot));

        List<WorkScheduleSlotResponse> result = schedulingService.getSlots(10L, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).staffId()).isEqualTo(20L);
    }

    @Test
    void shouldReturnSlotsByDateRange() {
        when(slotRepository.findByTenantIdAndDateRange(10L, now, later)).thenReturn(List.of(slot));

        List<WorkScheduleSlotResponse> result = schedulingService.getSlots(10L, now, later);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldAddSlot() {
        CreateWorkScheduleSlotRequest req = new CreateWorkScheduleSlotRequest(20L, 30L, null, now, later);
        when(slotRepository.save(any())).thenAnswer(inv -> {
            WorkScheduleSlot s = inv.getArgument(0);
            s.setId(99L);
            return s;
        });

        WorkScheduleSlotResponse response = schedulingService.addSlot(10L, req);

        assertThat(response.id()).isEqualTo(99L);
        verify(slotRepository).save(any(WorkScheduleSlot.class));
    }

    @Test
    void shouldThrowWhenAddSlotEndBeforeStart() {
        CreateWorkScheduleSlotRequest req = new CreateWorkScheduleSlotRequest(20L, 30L, null, later, now);

        assertThatThrownBy(() -> schedulingService.addSlot(10L, req))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void shouldDeleteSlot() {
        when(slotRepository.findById(1L)).thenReturn(Optional.of(slot));

        schedulingService.deleteSlot(10L, 1L);

        verify(slotRepository).delete(slot);
    }

    @Test
    void shouldThrowWhenDeleteSlotNotFound() {
        when(slotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> schedulingService.deleteSlot(10L, 99L))
                .isInstanceOf(ResponseStatusException.class);
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
