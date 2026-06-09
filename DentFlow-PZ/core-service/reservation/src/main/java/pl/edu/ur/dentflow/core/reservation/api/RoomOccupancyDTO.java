package pl.edu.ur.dentflow.core.reservation.api;

/**
 * DTO with occupancy data for a single room in a given time range.
 *
 * @param roomId           room identifier
 * @param totalSlotMinutes total available capacity in minutes (range * 1)
 * @param bookedMinutes    total minutes booked by appointments (sum of end-start)
 * @param occupancyPercent occupancy percentage (0-100)
 * @param appointmentCount number of appointments in range
 */
public record RoomOccupancyDTO(
        Long roomId,
        long totalSlotMinutes,
        long bookedMinutes,
        double occupancyPercent,
        long appointmentCount
) {}
