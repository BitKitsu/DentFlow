package pl.edu.ur.dentflow.core.reservation.application;

import pl.edu.ur.dentflow.core.clinic.domain.StaffMember;
import pl.edu.ur.dentflow.core.clinic.infrastructure.StaffMemberRepository;
import pl.edu.ur.dentflow.core.reservation.api.RoomOccupancyDTO;
import pl.edu.ur.dentflow.core.reservation.domain.Appointment;
import pl.edu.ur.dentflow.core.reservation.infrastructure.AppointmentRepository;
import pl.edu.ur.dentflow.pdf.model.RoomOccupancyReportData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoomOccupancyReportService {

    private final AppointmentRepository appointmentRepository;
    private final StaffMemberRepository staffMemberRepository;

    public RoomOccupancyReportService(AppointmentRepository appointmentRepository,
                                      StaffMemberRepository staffMemberRepository) {
        this.appointmentRepository = appointmentRepository;
        this.staffMemberRepository = staffMemberRepository;
    }

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

    @Transactional(readOnly = true)
    public RoomOccupancyDTO getRoomOccupancy(Long tenantId, Long roomId,
                                              OffsetDateTime from, OffsetDateTime to) {
        long totalSlotMinutes = Duration.between(from, to).toMinutes();
        return buildRoomStats(tenantId, roomId, from, to, totalSlotMinutes);
    }

    @Transactional(readOnly = true)
    public RoomOccupancyReportData buildReportData(Long tenantId, String clinicName,
                                                    OffsetDateTime from, OffsetDateTime to,
                                                    String locationFilter) {
        List<Appointment> allAppointments =
                appointmentRepository.findByTenantIdAndDateRange(tenantId, from, to);

        List<RoomOccupancyReportData.DailyStats> dailyStats = buildDailyStats(allAppointments, from);
        List<RoomOccupancyReportData.DoctorStats> doctorStats = buildDoctorStats(allAppointments, tenantId, from, to);
        double avgMinutes = computeAvgMinutes(allAppointments);
        List<String> topServices = computeTopServices(allAppointments);
        double noShowRate = computeNoShowRate(allAppointments);

        return new RoomOccupancyReportData(
                clinicName,
                from.getMonthValue(),
                from.getYear(),
                locationFilter,
                dailyStats,
                doctorStats,
                avgMinutes,
                topServices,
                noShowRate
        );
    }

    @Transactional(readOnly = true)
    public RoomOccupancyReportData buildSingleRoomReportData(Long tenantId, String clinicName,
                                                              Long roomId, OffsetDateTime from,
                                                              OffsetDateTime to) {
        List<Appointment> appointments =
                appointmentRepository.findByTenantIdAndRoomIdAndDateRange(tenantId, roomId, from, to);

        List<RoomOccupancyReportData.DailyStats> dailyStats = buildDailyStats(appointments, from);
        List<RoomOccupancyReportData.DoctorStats> doctorStats = buildDoctorStats(appointments, tenantId, from, to);
        double avgMinutes = computeAvgMinutes(appointments);
        List<String> topServices = computeTopServices(appointments);
        double noShowRate = computeNoShowRate(appointments);

        return new RoomOccupancyReportData(
                clinicName,
                from.getMonthValue(),
                from.getYear(),
                "Gabinet " + roomId,
                dailyStats,
                doctorStats,
                avgMinutes,
                topServices,
                noShowRate
        );
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

    private List<RoomOccupancyReportData.DailyStats> buildDailyStats(List<Appointment> appointments,
                                                                      OffsetDateTime from) {
        Map<Integer, Long> byDay = appointments.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getStartAt().getDayOfMonth(),
                        TreeMap::new,
                        Collectors.counting()));
        return byDay.entrySet().stream()
                .map(e -> new RoomOccupancyReportData.DailyStats(e.getKey(), e.getValue()))
                .toList();
    }

    private List<RoomOccupancyReportData.DoctorStats> buildDoctorStats(List<Appointment> appointments,
                                                                        Long tenantId,
                                                                        OffsetDateTime from,
                                                                        OffsetDateTime to) {
        Map<Long, List<Appointment>> byDentist = appointments.stream()
                .collect(Collectors.groupingBy(Appointment::getDentistStaffId));

        long totalSlotMinutes = Duration.between(from, to).toMinutes();

        return byDentist.entrySet().stream()
                .map(e -> {
                    Long staffId = e.getKey();
                    List<Appointment> apps = e.getValue();
                    String name = staffMemberRepository.findById(staffId)
                            .map(s -> s.getFirstName() + " " + s.getLastName())
                            .orElse("Lekarz ID: " + staffId);
                    long workMinutes = apps.stream()
                            .mapToLong(a -> Duration.between(a.getStartAt(), a.getEndAt()).toMinutes())
                            .sum();
                    double workHours = workMinutes / 60.0;
                    double utilization = totalSlotMinutes > 0
                            ? Math.min(100.0, (double) workMinutes / totalSlotMinutes * 100.0)
                            : 0.0;
                    return new RoomOccupancyReportData.DoctorStats(
                            name, apps.size(), workHours,
                            Math.round(utilization * 10.0) / 10.0);
                })
                .sorted((a, b) -> Long.compare(b.appointmentCount(), a.appointmentCount()))
                .toList();
    }

    private double computeAvgMinutes(List<Appointment> appointments) {
        if (appointments.isEmpty()) return 0.0;
        double avg = appointments.stream()
                .mapToLong(a -> Duration.between(a.getStartAt(), a.getEndAt()).toMinutes())
                .average()
                .orElse(0.0);
        return Math.round(avg * 10.0) / 10.0;
    }

    private List<String> computeTopServices(List<Appointment> appointments) {
        return appointments.stream()
                .filter(a -> a.getServiceItemId() != null)
                .collect(Collectors.groupingBy(
                        Appointment::getServiceItemId,
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(3)
                .map(e -> "Service ID: " + e.getKey())
                .toList();
    }

    private double computeNoShowRate(List<Appointment> appointments) {
        if (appointments.isEmpty()) return 0.0;
        long noShow = appointments.stream()
                .filter(a -> "CANCELLED".equals(a.getStatus()) || "NO_SHOW".equals(a.getStatus()))
                .count();
        return Math.round((double) noShow / appointments.size() * 1000.0) / 10.0;
    }
}
