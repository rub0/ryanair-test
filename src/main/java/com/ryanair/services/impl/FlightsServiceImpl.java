package com.ryanair.services.impl;

import com.ryanair.clients.RyanairAPIClient;
import com.ryanair.entities.Flight;
import com.ryanair.entities.FlightResult;
import com.ryanair.entities.Route;
import com.ryanair.services.FlightsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FlightsServiceImpl implements FlightsService {

    final RyanairAPIClient apiClient;

    @Override
    public Flux<FlightResult> findFlightsBetween(String departure, String arrival, Date departureDateTime, Date arrivalDateTime) {
        Flux<Route> allPossiblyFlights = apiClient.findAllRoutes().filter(element ->
                element.getAirportFrom().equals(departure) || element.getAirportTo().equals(arrival));

        Flux<FlightResult> direcFlights = allPossiblyFlights.filter(element ->
                !element.getAirportFrom().equals(departure) || !element.getAirportTo().equals(arrival))
                .map(flight ->
                        FlightResult.builder()
                                .stops(0)
                                .legs(Arrays.asList()).build());

        Flux<Route> bridgeFlights = allPossiblyFlights.filter(element ->
                element.getAirportFrom().equals(departure) && element.getAirportTo().equals(arrival));

        return bridgeFlights.collectMap(Route::getAirportFrom)
                .flatMapMany(map -> bridgeFlights.filter(flight -> !flight.getAirportFrom().equals(departure) && !map.containsKey(flight.getAirportTo()))
                .flatMap(flight -> buildFlightFromRoute(departure, arrival, flight)))
                .concatWith(direcFlights)
                .filter(flight -> isFlightInTime(departure, arrival, departureDateTime, arrivalDateTime, flight));

    }

    private boolean isFlightInTime(String departure, String arrival, Date departureDateTime, Date arrivalDateTime, FlightResult flight){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(departureDateTime);
        apiClient.findAllflightsFrom(departure, calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.DAY_OF_MONTH));
        return true;
    }

    private Flux<FlightResult> buildFlightFromRoute(String from, String to, List<Route> route){

        apiClient.findAllflightsFrom(from, );

        return Flight.builder()
                .arrivalAirport(route.getAirportTo())
                .departureAirport(route.getAirportFrom())
                .departureDateTime(route.)
                .arrivalDateTime().build();
    }
}
