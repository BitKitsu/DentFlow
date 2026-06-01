package com.dentflow.core.catalog.application;

import com.dentflow.core.catalog.api.CreateServiceCatalogItemRequest;
import com.dentflow.core.catalog.api.ServiceCatalogItemDTO;
import com.dentflow.core.catalog.api.UpdateServiceCatalogItemRequest;
import com.dentflow.core.catalog.domain.ServiceCatalogItem;
import com.dentflow.core.catalog.infrastructure.ServiceCatalogItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Serwis aplikacyjny obsługujący logikę biznesową cennika usług.
 *
 * <p>Wszystkie operacje są ograniczone do kontekstu konkretnego tenanta —
 * żadna metoda nie może odczytać ani zmodyfikować danych należących
 * do innego gabinetu.</p>
 *
 * <p>Gdy żądana usługa nie istnieje w kontekście tenanta, metody rzucają
 * {@link ResponseStatusException} z kodem {@code 404 Not Found}.</p>
 */
@Service
public class CatalogService {

    private final ServiceCatalogItemRepository repository;


    /**
     * Tworzy instancję serwisu z wstrzykniętym repozytorium.
     *
     * @param repository repozytorium pozycji cennika
     */
    public CatalogService(ServiceCatalogItemRepository repository) {
        this.repository = repository;
    }

    /**
     * Zwraca wszystkie usługi tenanta — zarówno aktywne, jak i nieaktywne.
     *
     * @param tenantId identyfikator tenanta
     * @return lista wszystkich {@link ServiceCatalogItemDTO} dla danego tenanta;
     *         pusta lista jeśli brak pozycji
     */
    @Transactional(readOnly = true)
    public List<ServiceCatalogItemDTO> getAllServices(Long tenantId) {
        return repository.findByTenantId(tenantId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Zwraca wyłącznie aktywne usługi tenanta.
     *
     * @param tenantId identyfikator tenanta
     * @return lista aktywnych {@link ServiceCatalogItemDTO};
     *         pusta lista jeśli brak aktywnych pozycji
     */
    @Transactional(readOnly = true)
    public List<ServiceCatalogItemDTO> getActiveServices(Long tenantId) {
        return repository.findByTenantIdAndActiveTrue(tenantId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Zwraca pojedynczą usługę tenanta po jej identyfikatorze.
     *
     * @param tenantId identyfikator tenanta
     * @param id       identyfikator usługi
     * @return {@link ServiceCatalogItemDTO} dla znalezionej usługi
     * @throws ResponseStatusException {@code 404 Not Found} gdy usługa
     *         o podanym {@code id} nie istnieje w kontekście tenanta
     */

    @Transactional(readOnly = true)
    public ServiceCatalogItemDTO getService(Long tenantId, Long id) {
        return repository.findByIdAndTenantId(id, tenantId)
                .map(this::toDTO)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usługa o id=" + id + " nie istnieje w tym gabinecie"));
    }

    /**
     * Tworzy nową pozycję w cenniku tenanta.
     *
     * <p>Jeśli pole {@code active} w żądaniu ma wartość {@code null},
     * usługa jest domyślnie tworzona jako aktywna.</p>
     *
     * @param tenantId identyfikator tenanta
     * @param request  dane nowej usługi
     * @return {@link ServiceCatalogItemDTO} reprezentujący zapisaną usługę
     */

    @Transactional
    public ServiceCatalogItemDTO createService(Long tenantId, CreateServiceCatalogItemRequest request) {
        ServiceCatalogItem item = ServiceCatalogItem.builder()
                .tenantId(tenantId)
                .name(request.name())
                .durationMinutes(request.durationMinutes())
                .priceCents(request.priceCents())
                .active(request.active() != null ? request.active() : true)
                .build();
        return toDTO(repository.save(item));
    }
    /**
     * Aktualizuje wszystkie pola istniejącej usługi tenanta.
     *
     * <p>Metoda nadpisuje wszystkie edytowalne pola wartościami z {@code request} —
     * każde pole musi być podane nawet jeśli się nie zmienia.</p>
     *
     * @param tenantId identyfikator tenanta
     * @param id       identyfikator usługi do zaktualizowania
     * @param request  nowe dane usługi
     * @return {@link ServiceCatalogItemDTO} z zaktualizowanymi danymi
     * @throws ResponseStatusException {@code 404 Not Found} gdy usługa
     *         o podanym {@code id} nie istnieje w kontekście tenanta
     */
    @Transactional
    public ServiceCatalogItemDTO updateService(Long tenantId, Long id, UpdateServiceCatalogItemRequest request) {
        ServiceCatalogItem item = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usługa o id=" + id + " nie istnieje w tym gabinecie"));

        item.setName(request.name());
        item.setDurationMinutes(request.durationMinutes());
        item.setPriceCents(request.priceCents());
        item.setActive(request.active());

        return toDTO(repository.save(item));
    }

    /**
     * Usuwa pozycję z cennika tenanta.
     *
     * @param tenantId identyfikator tenanta
     * @param id       identyfikator usługi do usunięcia
     * @throws ResponseStatusException {@code 404 Not Found} gdy usługa
     *         o podanym {@code id} nie istnieje w kontekście tenanta
     */
    @Transactional
    public void deleteService(Long tenantId, Long id) {
        ServiceCatalogItem item = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usługa o id=" + id + " nie istnieje w tym gabinecie"));
        repository.delete(item);
    }

    /**
     * Mapuje encję {@link ServiceCatalogItem} na obiekt transferu danych.
     *
     * @param item encja do zmapowania
     * @return odpowiadający {@link ServiceCatalogItemDTO}
     */
    private ServiceCatalogItemDTO toDTO(ServiceCatalogItem item) {
        return new ServiceCatalogItemDTO(
                item.getId(),
                item.getTenantId(),
                item.getName(),
                item.getDurationMinutes(),
                item.getPriceCents(),
                item.getActive()
        );
    }
}
