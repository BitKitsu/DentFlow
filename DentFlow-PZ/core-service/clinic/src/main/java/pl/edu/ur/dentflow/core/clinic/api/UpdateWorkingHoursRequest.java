package pl.edu.ur.dentflow.core.clinic.api;

import java.util.List;

public record UpdateWorkingHoursRequest(
        List<WorkingHoursEntry> schedule
) {}
