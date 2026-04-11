package com.dentflow.core.clinic.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateLocationRequest(
        @NotBlank @Size(max = 100)
        String name,
        
        @Size(max = 100)
        String addressStreet,
        
        @Size(max = 100)
        String addressCity,
        
        @Size(max = 20)
        String addressZip,
        
        @Size(max = 50)
        String addressCountry
) {}
