package com.sobolev.travel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sobolev.travel.dto.trip.*;
import com.sobolev.travel.entity.*;
import com.sobolev.travel.exception.BadRequestException;
import com.sobolev.travel.exception.ResourceNotFoundException;
import com.sobolev.travel.mapper.EntityMapper;
import com.sobolev.travel.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * Servizio che gestisce la logica di business per i viaggi (Trip) e le tappe (TripStop).
 *
 * Responsabilità principali:
 * - CRUD sui viaggi per utente (creazione, aggiornamento, cancellazione, lista)
 * - Aggiunta/aggiornamento/rimozione delle tappe di un viaggio
 * - Esportazione di un viaggio in formato CSV
 *
 * Motivazioni progettuali e scelte:
 * - Le operazioni sono transazionali per garantire coerenza (ad es. aggiunta di tappa e salvataggio del viaggio).
 * - Alcune query (definite nei repository) usano LEFT JOIN FETCH per evitare problemi N+1 quando si serializzano DTO.
 * - Vengono fatte validazioni sui range di date (start/end del viaggio e date delle tappe) per mantenere invarianti.
 */
@Service
public class TripService {

    private final TripRepository tripRepository;
    private final TripStopRepository tripStopRepository;
    private final CityRepository cityRepository;
    private final UserRepository userRepository;
    private final PoiRepository poiRepository;
    private final EntityMapper mapper;
    private final ObjectMapper objectMapper;

    public TripService(TripRepository tripRepository,
                       TripStopRepository tripStopRepository,
                       CityRepository cityRepository,
                       UserRepository userRepository,
                       PoiRepository poiRepository,
                       EntityMapper mapper,
                       ObjectMapper objectMapper) {
        this.tripRepository = tripRepository;
        this.tripStopRepository = tripStopRepository;
        this.cityRepository = cityRepository;
        this.userRepository = userRepository;
        this.poiRepository = poiRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    /**
     * Recupera tutti i viaggi dell'utente ordinati per data di inizio desc.
     * Usato per mostrare la lista dei viaggi nella UI.
     */
    @Transactional(readOnly = true)
    public List<TripDto> getUserTrips(Integer userId) {
        return tripRepository.findByUserIdOrderByStartDateDesc(userId).stream()
            .map(mapper::toTripDto)
            .toList();
    }

    /**
     * Recupera un viaggio per nome (case-insensitive) dell'utente.
     * Lancia ResourceNotFoundException se non esiste.
     */
    @Transactional(readOnly = true)
    public TripDto getTripByName(String tripName, Integer userId) {
        Trip trip = tripRepository.findByNameAndUserIdIgnoreCase(tripName, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Trip '" + tripName + "' not found for user"));
        return mapper.toTripDto(trip);
    }

    /**
     * Crea un nuovo viaggio per l'utente.
     * Valida le date (start <= end) prima della persistenza.
     */
    @Transactional
    public TripDto createTrip(TripCreateRequest request, Integer userId) {
        validateTripDates(request.startDate(), request.endDate());

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Trip trip = new Trip();
        trip.setUser(user);
        trip.setName(request.name());
        trip.setStartDate(request.startDate());
        trip.setEndDate(request.endDate());

        trip = tripRepository.save(trip);
        return mapper.toTripDto(trip);
    }

    /**
     * Aggiorna i campi di un viaggio esistente (nome, date). Dopo l'update valida
     * che tutte le tappe esistenti siano still coerenti con il nuovo range di date.
     */
    @Transactional
    public TripDto updateTrip(String tripName, TripUpdateRequest request, Integer userId) {
        Trip trip = tripRepository.findByNameAndUserIdIgnoreCase(tripName, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Trip '" + tripName + "' not found for user"));

        if (request.name() != null) {
            trip.setName(request.name());
        }
        if (request.startDate() != null) {
            trip.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            trip.setEndDate(request.endDate());
        }

        validateTripDates(trip.getStartDate(), trip.getEndDate());

        for (TripStop stop : trip.getStops()) {
            validateStopDate(stop.getStopDate(), trip.getStartDate(), trip.getEndDate());
        }

        trip = tripRepository.save(trip);
        return mapper.toTripDto(trip);
    }

    /**
     * Elimina un viaggio dell'utente. Le cascade definite sulle entity rimuovono anche le tappe.
     */
    @Transactional
    public void deleteTrip(String tripName, Integer userId) {
        Trip trip = tripRepository.findByNameAndUserIdIgnoreCase(tripName, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Trip '" + tripName + "' not found for user"));
        tripRepository.delete(trip);
    }

    /**
     * Aggiunge una tappa a un viaggio.
     * - Valida che la data della tappa sia nel range del viaggio.
     * - Risolve la città (eventualmente lancia ResourceNotFoundException o BadRequest se ambigua).
     * - Genera uno stopName univoco basato su nome città e data per evitare duplicati.
     */
    @Transactional
    public TripStopDto addStop(String tripName, TripStopCreateRequest request, Integer userId) {
        Trip trip = tripRepository.findByNameAndUserIdIgnoreCase(tripName, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Trip '" + tripName + "' not found for user"));

        validateStopDate(request.stopDate(), trip.getStartDate(), trip.getEndDate());

        City city = findCityByName(request.cityName(), request.regionName());

        String stopName = generateStopName(city.getName(), request.stopDate());

        if (tripStopRepository.findByStopNameAndTripId(stopName, trip.getId()).isPresent()) {
            throw new BadRequestException("A stop for " + city.getName() + " on " + request.stopDate() + " already exists in this trip");
        }

        TripStop stop = new TripStop();
        stop.setTrip(trip);
        stop.setCity(city);
        stop.setStopName(stopName);
        stop.setStopDate(request.stopDate());
        stop.setNotes(request.notes());

        trip.addStop(stop);
        tripRepository.save(trip);

        return mapper.toTripStopDto(stop);
    }

    /**
     * Aggiorna una tappa esistente. Se la data cambia viene anche aggiornato lo stopName
     * e controllata la collisione con altre tappe.
     */
    @Transactional
    public TripStopDto updateStop(String tripName, String stopName, TripStopUpdateRequest request, Integer userId) {
        TripStop stop = tripStopRepository.findByStopNameAndTripNameAndUserId(stopName, tripName, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Stop '" + stopName + "' not found in trip '" + tripName + "'"));

        Trip trip = stop.getTrip();

        if (request.stopDate() != null) {
            validateStopDate(request.stopDate(), trip.getStartDate(), trip.getEndDate());

            String newStopName = generateStopName(stop.getCity().getName(), request.stopDate());
            if (!newStopName.equalsIgnoreCase(stop.getStopName())) {
                if (tripStopRepository.findByStopNameAndTripId(newStopName, trip.getId()).isPresent()) {
                    throw new BadRequestException("A stop for " + stop.getCity().getName() + " on " + request.stopDate() + " already exists in this trip");
                }
                stop.setStopName(newStopName);
            }
            stop.setStopDate(request.stopDate());
        }
        if (request.notes() != null) {
            stop.setNotes(request.notes());
        }

        tripStopRepository.save(stop);
        return mapper.toTripStopDto(stop);
    }

    /**
     * Rimuove una tappa da un viaggio; mantiene la coerenza bidirezionale rimuovendo la tappa
     * dal viaggio e salvando il viaggio aggiornato.
     */
    @Transactional
    public void deleteStop(String tripName, String stopName, Integer userId) {
        TripStop stop = tripStopRepository.findByStopNameAndTripNameAndUserId(stopName, tripName, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Stop '" + stopName + "' not found in trip '" + tripName + "'"));

        Trip trip = stop.getTrip();
        trip.removeStop(stop);
        tripRepository.save(trip);
    }


    /**
     * Esporta un viaggio in CSV. Il CSV viene costruito attraversando tutte le tappe
     * e le righe per ogni POI (o una riga vuota per POI se non presenti).
     */
    @Transactional(readOnly = true)
    public String exportTripToCsv(String tripName, Integer userId) {
        Trip trip = tripRepository.findByNameAndUserIdIgnoreCase(tripName, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Trip '" + tripName + "' not found for user"));

        return generateCsvFromTrip(trip);
    }

    /**
     * Costruisce il CSV da un oggetto Trip. Implementazione semplice e robusta:
     * - Escape delle virgolette
     * - Una riga per ogni tappa del viaggio
     */
    private String generateCsvFromTrip(Trip trip) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("Trip Name,Start Date,End Date,Stop Name,Stop Date,City,Region,Notes");

        for (TripStop stop : trip.getStops()) {
            pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                escapeCSV(trip.getName()),
                trip.getStartDate(),
                trip.getEndDate(),
                escapeCSV(stop.getStopName()),
                stop.getStopDate(),
                escapeCSV(stop.getCity().getName()),
                escapeCSV(stop.getCity().getRegion().getName()),
                escapeCSV(stop.getNotes() != null ? stop.getNotes() : "")
            );
        }

        return sw.toString();
    }

    /**
     * Trova una città per nome e (opzionalmente) nome della regione.
     * - Se vengono trovate più città con lo stesso nome e non è passata la regione,
     *   viene sollevata una BadRequestException per richiedere all'utente di specificare la regione.
     */
    private City findCityByName(String cityName, String regionName) {
        if (regionName != null && !regionName.isBlank()) {
            return cityRepository.findByCityNameAndRegionNameIgnoreCase(cityName, regionName)
                .orElseThrow(() -> new ResourceNotFoundException("City '" + cityName + "' in region '" + regionName + "' not found"));
        } else {
            List<City> cities = cityRepository.findByNameIgnoreCaseWithDetails(cityName);
            if (cities.isEmpty()) {
                throw new ResourceNotFoundException("City '" + cityName + "' not found");
            }
            if (cities.size() > 1) {
                throw new BadRequestException("Multiple cities found with name '" + cityName + "'. Please specify the region.");
            }
            return cities.get(0);
        }
    }

    /**
     * Genera uno stopName univoco a partire da nome città e data. Questo semplifica
     * l'identificazione delle tappe e la prevenzione di duplicati.
     */
    private String generateStopName(String cityName, java.time.LocalDate date) {
        return cityName.toLowerCase().replaceAll("\\s+", "_") + "_" + date.toString();
    }

    /**
     * Validazione che la data di inizio sia <= data di fine.
     */
    private void validateTripDates(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date must be before or equal to end date");
        }
    }

    /**
     * Validazione che la data della tappa cada nel range del viaggio.
     */
    private void validateStopDate(java.time.LocalDate stopDate, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (stopDate.isBefore(startDate) || stopDate.isAfter(endDate)) {
            throw new BadRequestException("Stop date must be within trip date range");
        }
    }

    /**
     * Semplice escape per CSV: raddoppia le virgolette interne.
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
