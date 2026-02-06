package com.sobolev.travel.service;

import com.sobolev.travel.dto.geography.CountryDto;
import com.sobolev.travel.entity.Country;
import com.sobolev.travel.exception.ResourceNotFoundException;
import com.sobolev.travel.mapper.EntityMapper;
import com.sobolev.travel.repository.CountryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servizio per operazioni di lettura sui paesi (Country).
 *
 * Espone metodi per recuperare la lista completa e per cercare un paese per nome
 * (case-insensitive). I dati vengono convertiti in DTO tramite `EntityMapper`.
 */
@Service
public class CountryService {

    private final CountryRepository countryRepository;
    private final EntityMapper mapper;

    public CountryService(CountryRepository countryRepository, EntityMapper mapper) {
        this.countryRepository = countryRepository;
        this.mapper = mapper;
    }

    /**
     * Recupera tutti i paesi.
     *
     * @return lista di tutti i paesi come {@link CountryDto}
     */
    @Transactional(readOnly = true)
    public List<CountryDto> getAllCountries() {
        return countryRepository.findAll().stream()
            .map(mapper::toCountryDto)
            .toList();
    }

    /**
     * Recupera un paese per nome.
     *
     * @param name nome del paese da cercare
     * @return il paese corrispondente come {@link CountryDto}
     * @throws ResourceNotFoundException se il paese non viene trovato
     */
    @Transactional(readOnly = true)
    public CountryDto getCountryByName(String name) {
        Country country = countryRepository.findByNameIgnoreCase(name)
            .orElseThrow(() -> new ResourceNotFoundException("Country '" + name + "' not found"));
        return mapper.toCountryDto(country);
    }
}
