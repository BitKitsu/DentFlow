package com.dentflow.core.clinic.api;

import java.time.LocalTime;

public record StaffWorkingHoursDTO(
        Long id,
        Long staffMemberId,
        Integer dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {}
