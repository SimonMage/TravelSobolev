package com.sobolev.travel.service;

import com.sobolev.travel.dto.poi.PoiCreateRequest;
import com.sobolev.travel.dto.poi.PoiDto;
import com.sobolev.travel.entity.Poi;
import com.sobolev.travel.entity.User;
import com.sobolev.travel.exception.ResourceNotFoundException;
import com.sobolev.travel.mapper.EntityMapper;
import com.sobolev.travel.repository.PoiRepository;
import com.sobolev.travel.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servizio per la gestione dei POI personalizzati degli utenti.
 *
 * Fornisce metodi per creare, recuperare ed eliminare i punti di interesse
 * definiti dagli utenti.
 */
@Service
public class PoiService {

    private final PoiRepository poiRepository;
    private final UserRepository userRepository;
    private final EntityMapper mapper;

    public PoiService(PoiRepository poiRepository, UserRepository userRepository, EntityMapper mapper) {
        this.poiRepository = poiRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    /**
     * Crea un nuovo POI personalizzato per l'utente.
     *
     * @param userId  ID dell'utente
     * @param request dati del POI da creare
     * @return il POI creato
     */
    @Transactional
    public PoiDto createPoi(Integer userId, PoiCreateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verifica che non esista già un POI con lo stesso nome per questo utente
        poiRepository.findByUserIdAndNameIgnoreCase(userId, request.name())
            .ifPresent(poi -> {
                throw new IllegalArgumentException("POI with name '" + request.name() + "' already exists for this user");
            });

        Poi poi = new Poi();
        poi.setUser(user);
        poi.setName(request.name());
        poi.setDescription(request.description());
        poi.setLatitude(request.latitude());
        poi.setLongitude(request.longitude());

        Poi saved = poiRepository.save(poi);
        return mapper.toPoiDto(saved);
    }

    /**
     * Restituisce tutti i POI di un utente.
     *
     * @param userId ID dell'utente
     * @return lista dei POI dell'utente
     */
    @Transactional(readOnly = true)
    public List<PoiDto> getUserPois(Integer userId) {
        return poiRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(mapper::toPoiDto)
            .toList();
    }

    /**
     * Restituisce un POI specifico per ID.
     *
     * @param userId ID dell'utente
     * @param poiId  ID del POI
     * @return il POI richiesto
     */
    @Transactional(readOnly = true)
    public PoiDto getPoiById(Integer userId, Integer poiId) {
        Poi poi = poiRepository.findById(poiId)
            .orElseThrow(() -> new ResourceNotFoundException("POI not found"));

        // Verifica che il POI appartenga all'utente
        if (!poi.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("POI not found");
        }

        return mapper.toPoiDto(poi);
    }

    /**
     * Elimina un POI specifico.
     *
     * @param userId ID dell'utente
     * @param poiId  ID del POI da eliminare
     */
    @Transactional
    public void deletePoi(Integer userId, Integer poiId) {
        Poi poi = poiRepository.findById(poiId)
            .orElseThrow(() -> new ResourceNotFoundException("POI not found"));

        // Verifica che il POI appartenga all'utente
        if (!poi.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("POI not found");
        }

        poiRepository.delete(poi);
    }

    /**
     * Elimina tutti i POI di un utente.
     *
     * @param userId ID dell'utente
     */
    @Transactional
    public void deleteAllUserPois(Integer userId) {
        poiRepository.deleteByUserId(userId);
    }
}

