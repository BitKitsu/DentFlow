package com.dentflow.identity.auth.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
/**
 * Żądanie przypisania gabinetu (tenanta) do konta zalogowanego użytkownika.
 *
 * <p>Używane przez endpoint {@code POST /auth/tenant}.
 * Wywoływane po pomyślnym utworzeniu gabinetu w osobnym module,
 * gdy wygenerowany {@code tenantId} musi zostać powiązany z kontem właściciela.</p>
 *
 * @param tenantId identyfikator gabinetu do przypisania; wymagany, musi być większy od 0
 */
public record AssignTenantRequest(
        @NotNull @Min(1) Long tenantId
) {}
