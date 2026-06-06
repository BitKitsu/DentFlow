package com.dentflow.core.clinic.api;

import java.time.LocalTime;

public record WorkingHoursEntry(
        Integer dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {}
