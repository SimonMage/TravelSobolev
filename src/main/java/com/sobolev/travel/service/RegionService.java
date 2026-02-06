package com.sobolev.travel.service;

import com.sobolev.travel.dto.geography.RegionDto;
import com.sobolev.travel.entity.Region;
import com.sobolev.travel.exception.ResourceNotFoundException;
import com.sobolev.travel.mapper.EntityMapper;
import com.sobolev.travel.repository.CountryRepository;
import com.sobolev.travel.repository.RegionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servizio per operazioni sulle regioni.
 *
 * Fornisce metodi per ottenere tutte le regioni, le regioni di un paese e
 * per cercare una regione per nome (con possibile specifica del paese).
 */
@Service
public class RegionService {

    private final RegionRepository regionRepository;
    private final CountryRepository countryRepository;
    private final EntityMapper mapper;

    public RegionService(RegionRepository regionRepository, CountryRepository countryRepository, EntityMapper mapper) {
        this.regionRepository = regionRepository;
        this.countryRepository = countryRepository;
        this.mapper = mapper;
    }

    /**
     * Restituisce tutte le regioni.
     *
     * @return lista di tutte le regioni
     */
    @Transactional(readOnly = true)
    public List<RegionDto> getAllRegions() {
        return regionRepository.findAll().stream()
            .map(mapper::toRegionDto)
            .toList();
    }

    /**
     * Restituisce le regioni di un paese specificato per ID.
     *
     * @param countryId ID del paese
     * @return lista delle regioni del paese
     */
    @Transactional(readOnly = true)
    public List<RegionDto> getRegionsByCountry(Integer countryId) {
        return regionRepository.findByCountryId(countryId).stream()
            .map(mapper::toRegionDto)
            .toList();
    }

    /**
     * Restituisce le regioni di un paese specificato per nome.
     *
     * @param countryName nome del paese
     * @return lista delle regioni del paese
     */
    @Transactional(readOnly = true)
    public List<RegionDto> getRegionsByCountryName(String countryName) {
        var country = countryRepository.findByNameIgnoreCase(countryName)
            .orElseThrow(() -> new ResourceNotFoundException("Country '" + countryName + "' not found"));
        return regionRepository.findByCountryId(country.getId()).stream()
            .map(mapper::toRegionDto)
            .toList();
    }

    /**
     * Restituisce una regione cercando per nome regione e nome paese.
     * Se il nome del paese non è fornito, cerca solo per nome regione
     * (può restituire più risultati, viene preso il primo).
     *
     * @param regionName  nome della regione
     * @param countryName nome del paese (opzionale)
     * @return la regione trovata
     */
    @Transactional(readOnly = true)
    public RegionDto getRegionByName(String regionName, String countryName) {
        Region region;

        if (countryName != null && !countryName.isBlank()) {
            // Ricerca per nome regione e nome paese
            region = regionRepository.findByNameAndCountryNameIgnoreCase(regionName, countryName)
                .orElseThrow(() -> new ResourceNotFoundException("Region '" + regionName + "' in country '" + countryName + "' not found"));
        } else {
            // Ricerca solo per nome regione (può restituire più risultati, viene preso il primo)
            List<Region> regions = regionRepository.findByNameIgnoreCaseWithCountry(regionName);
            if (regions.isEmpty()) {
                throw new ResourceNotFoundException("Region '" + regionName + "' not found");
            }
            region = regions.get(0);
        }

        return mapper.toRegionDto(region);
    }
}
