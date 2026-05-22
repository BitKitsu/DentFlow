package com.dentflow.core.reservation.api;

/**
 * DTO z danymi obłożenia jednego gabinetu/pokoju w zadanym przedziale czasu.
 *
 * @param roomId           identyfikator pokoju
 * @param totalSlotMinutes całkowita dostępna pojemność w minutach (przedział * 1)
 * @param bookedMinutes    łączna liczba minut zajętych przez wizyty (suma end-start)
 * @param occupancyPercent procent obłożenia (0-100)
 * @param appointmentCount liczba wizyt w przedziale
 */
public record RoomOccupancyDTO(
        Long roomId,
        long totalSlotMinutes,
        long bookedMinutes,
        double occupancyPercent,
        long appointmentCount
) {}
