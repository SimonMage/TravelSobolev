package com.sobolev.travel.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sobolev.travel.dto.geography.*;
import com.sobolev.travel.dto.poi.PoiDto;
import com.sobolev.travel.dto.trip.*;
import com.sobolev.travel.dto.user.UserDto;
import com.sobolev.travel.dto.user.UserProfileDto;
import com.sobolev.travel.entity.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Mapper centralizzato che converte le entity JPA in DTO utilizzati dalle API.
 *
 * Motivazione:
 * - Separare le dipendenze di persistenza dal modello esposto tramite le API.
 * - Centralizzare la logica di mapping per riuso e test.
 */
@Component
public class EntityMapper {

    private final ObjectMapper objectMapper;

    public EntityMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Converte un'entity User in un UserDto.
     *
     * @param user l'entity User da convertire
     * @return il corrispondente UserDto
     */
    public UserDto toUserDto(User user) {
        UserProfileDto profileDto = null;
        if (user.getProfile() != null) {
            profileDto = new UserProfileDto(
                user.getUsername(),
                user.getEmail(),
                user.getProfile().getFirstName(),
                user.getProfile().getLastName(),
                user.getProfile().getPreferredUnits()
            );
        }
        return new UserDto(
            user.getId(),
            user.getCreatedAt(),
            profileDto
        );
    }

    /**
     * Converte un'entity Country in un CountryDto.
     *
     * @param country l'entity Country da convertire
     * @return il corrispondente CountryDto
     */
    public CountryDto toCountryDto(Country country) {
        return new CountryDto(country.getId(), country.getName());
    }

    /**
     * Converte un'entity Region in un RegionDto.
     *
     * @param region l'entity Region da convertire
     * @return il corrispondente RegionDto
     */
    public RegionDto toRegionDto(Region region) {
        return new RegionDto(
            region.getId(),
            region.getName(),
            region.getCountry().getId(),
            region.getCountry().getName()
        );
    }

    /**
     * Converte un'entity City in un CityDto.
     *
     * @param city l'entity City da convertire
     * @return il corrispondente CityDto
     */
    public CityDto toCityDto(City city) {
        List<String> tags = city.getTags() != null
            ? city.getTags().stream().map(Tag::getName).toList()
            : Collections.emptyList();

        return new CityDto(
            city.getId(),
            city.getName(),
            city.getLatitude(),
            city.getLongitude(),
            city.getRegion().getId(),
            city.getRegion().getName(),
            city.getRegion().getCountry().getName(),
            tags
        );
    }

    /**
     * Converte un'entity Tag in un TagDto.
     *
     * @param tag l'entity Tag da convertire
     * @return il corrispondente TagDto
     */
    public TagDto toTagDto(Tag tag) {
        return new TagDto(tag.getId(), tag.getName());
    }

    /**
     * Converte un'entity SearchHistory in un SearchHistoryDto.
     *
     * @param searchHistory l'entity SearchHistory da convertire
     * @return il corrispondente SearchHistoryDto
     */
    public SearchHistoryDto toSearchHistoryDto(SearchHistory searchHistory) {
        return new SearchHistoryDto(
            searchHistory.getId(),
            searchHistory.getQuery(),
            searchHistory.getCity() != null ? searchHistory.getCity().getId() : null,
            searchHistory.getCity() != null ? searchHistory.getCity().getName() : null,
            searchHistory.getSearchedAt()
        );
    }

    /**
     * Converte un'entity Trip in un TripDto.
     *
     * @param trip l'entity Trip da convertire
     * @return il corrispondente TripDto
     */
    public TripDto toTripDto(Trip trip) {
        List<TripStopDto> stops = trip.getStops() != null
            ? trip.getStops().stream().map(this::toTripStopDto).toList()
            : Collections.emptyList();

        return new TripDto(
            trip.getId(),
            trip.getName(),
            trip.getStartDate(),
            trip.getEndDate(),
            stops
        );
    }

    /**
     * Converte un'entity TripStop in un TripStopDto.
     *
     * @param stop l'entity TripStop da convertire
     * @return il corrispondente TripStopDto
     */
    public TripStopDto toTripStopDto(TripStop stop) {
        return new TripStopDto(
            stop.getStopName(),
            stop.getCity().getName(),
            stop.getCity().getRegion().getName(),
            stop.getStopDate(),
            stop.getNotes()
        );
    }

    /**
     * Converte un'entity Poi in un PoiDto.
     *
     * @param poi l'entity Poi da convertire
     * @return il corrispondente PoiDto
     */
    public PoiDto toPoiDto(Poi poi) {
        return new PoiDto(
            poi.getId(),
            poi.getName(),
            poi.getDescription(),
            poi.getLatitude(),
            poi.getLongitude(),
            poi.getCreatedAt()
        );
    }
}
