package com.dentflow.core.reservation.application;

import com.dentflow.core.reservation.api.RoomOccupancyDTO;
import com.dentflow.core.reservation.domain.Appointment;
import com.dentflow.core.reservation.infrastructure.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class RoomOccupancyReportService {

    private final AppointmentRepository appointmentRepository;

    public RoomOccupancyReportService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Generuje raport obłożenia wszystkich gabinetów dla danego tenanta
     * w przedziale czasowym [from, to).
     *
     * Procent obłożenia = suma czasu trwania wizyt / całkowita długość przedziału * 100
     */
    @Transactional(readOnly = true)
    public List<RoomOccupancyDTO> getRoomOccupancyReport(Long tenantId,
                                                          OffsetDateTime from,
                                                          OffsetDateTime to) {
        long totalSlotMinutes = Duration.between(from, to).toMinutes();

        List<Long> roomIds = appointmentRepository.findDistinctRoomIdsByTenantIdAndDateRange(tenantId, from, to);

        return roomIds.stream()
                .map(roomId -> buildRoomStats(tenantId, roomId, from, to, totalSlotMinutes))
                .sorted((a, b) -> Double.compare(b.occupancyPercent(), a.occupancyPercent()))
                .toList();
    }

    /**
     * Generuje raport obłożenia konkretnego gabinetu w przedziale [from, to).
     */
    @Transactional(readOnly = true)
    public RoomOccupancyDTO getRoomOccupancy(Long tenantId, Long roomId,
                                              OffsetDateTime from, OffsetDateTime to) {
        long totalSlotMinutes = Duration.between(from, to).toMinutes();
        return buildRoomStats(tenantId, roomId, from, to, totalSlotMinutes);
    }

    private RoomOccupancyDTO buildRoomStats(Long tenantId, Long roomId,
                                             OffsetDateTime from, OffsetDateTime to,
                                             long totalSlotMinutes) {
        List<Appointment> appointments =
                appointmentRepository.findByTenantIdAndRoomIdAndDateRange(tenantId, roomId, from, to);

        long bookedMinutes = appointments.stream()
                .mapToLong(a -> Duration.between(a.getStartAt(), a.getEndAt()).toMinutes())
                .sum();

        double occupancyPercent = totalSlotMinutes > 0
                ? Math.min(100.0, (double) bookedMinutes / totalSlotMinutes * 100.0)
                : 0.0;

        return new RoomOccupancyDTO(
                roomId,
                totalSlotMinutes,
                bookedMinutes,
                Math.round(occupancyPercent * 10.0) / 10.0,
                appointments.size()
        );
    }
}
