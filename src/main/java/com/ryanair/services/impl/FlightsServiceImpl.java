package com.ryanair.services.impl;

import com.ryanair.clients.RyanairAPIClient;
import com.ryanair.entities.Day;
import com.ryanair.entities.Flight;
import com.ryanair.entities.FlightResult;
import com.ryanair.entities.Route;
import com.ryanair.services.FlightsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                element.getAirportFrom().equals(departure) && element.getAirportTo().equals(arrival))
                .flatMap(route -> getFlightResultFromRoute(route, arrivalDateTime, departureDateTime));

        Flux<Route> bridgeFlights = allPossiblyFlights.filter(element ->
                element.getAirportFrom().equals(departure) && element.getAirportTo().equals(arrival)
                || element.getAirportFrom().equals(departure) && !element.getAirportTo().equals(arrival)
                || !element.getAirportFrom().equals(departure) && element.getAirportTo().equals(arrival));

        return bridgeFlights.collectMap(route -> route.getAirportFrom() + route.getAirportTo())
                .flatMapMany(map ->
                        allPossiblyFlights
                                .filter(flight ->
                                        map.containsKey(flight.getAirportFrom() + flight.getAirportTo())
                                && (map.containsKey(departure + flight.getAirportTo()) && map.containsKey(flight.getAirportTo() + arrival)))
                                .flatMap(flight ->
                                        buildFlightResultFromRoute(
                                                departure,
                                                arrival,
                                                Arrays.asList(flight, map.get(flight.getAirportTo() + arrival)),
                                                departureDateTime,
                                                arrivalDateTime))
                ).concatWith(direcFlights)
                 .filter(flight -> isValidFlight(departureDateTime, arrivalDateTime, flight))
                .filter(flight ->
                        isBridgeInTime(flight, departure, arrival));

    }

    public boolean isBridgeInTime(FlightResult flight, String departure, String arrival){
        Flight aux = flight.getLegs().get(0);
        Flight dep = aux.getDepartureAirport().equals(departure) ? aux : flight.getLegs().get(1);
        Flight arr = aux.getArrivalAirport().equals(arrival) ? aux : flight.getLegs().get(1);
        return flight.getStops() == 0
                || DateUtils.addHours(dep.getArrivalDateTime(), 2).getTime()
                    < arr.getDepartureDateTime().getTime();
    }

    public boolean isValidFlight(Date departureDateTime, Date arrivalDateTime, FlightResult flight) {
        return flight.getStops() + 1 == flight.getLegs().size()
                && flight.getLegs().stream().map(leg ->
                    leg.getDepartureDateTime().getTime() > departureDateTime.getTime() && leg.getArrivalDateTime().getTime() < arrivalDateTime.getTime()
                ).reduce(true, (res, leg) -> res && leg);
    }

    public Mono<FlightResult> buildFlightResultFromRoute(String from, String to, List<Route> routes, Date departureDateTime, Date arrivalDateTime) {
        return Flux.fromIterable(routes.stream().map(route ->
            getFlightResultFromRoute(route, arrivalDateTime, departureDateTime)
        ).collect(Collectors.toList())).flatMap(x -> x).reduce(FlightResult.builder().legs(Collections.emptyList()).build(), (result, leg) ->
                FlightResult.builder()
                        .stops(1)
                        .legs(Stream.concat(result.getLegs().stream(), leg.getLegs().stream()).collect(Collectors.toList())).build());
    }

    public Flux<FlightResult> getFlightResultFromRoute(Route route, Date arrivalDateTime, Date departureDateTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(departureDateTime);

        return apiClient.findAllflightsFrom(route.getAirportFrom(), route.getAirportTo(), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
                .flatMap(schedule ->
                        Flux.fromIterable(schedule.getDays().stream().filter(day ->
                                        day.getDay().equals(Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)))
                                ).map(day ->
                                        Day.builder().day(day.getDay()).flights(day.getFlights().stream().filter(date -> {
                                            long timestamp = Date.from(LocalDateTime.of(
                                                    calendar.get(Calendar.YEAR),
                                                    calendar.get(Calendar.MONTH) + 1,
                                                    calendar.get(Calendar.DAY_OF_MONTH),
                                                    Integer.parseInt(date.getDepartureTime().split(":")[0]),
                                                    Integer.parseInt(date.getDepartureTime().split(":")[1])).atZone(ZoneId.systemDefault()).toInstant()).getTime();
                                            return calendar.getTimeInMillis() < timestamp && timestamp < arrivalDateTime.getTime();
                                        }).collect(Collectors.toList()))
                                                .day(day.getDay())
                                                .build())
                                        .filter(day -> !day.getFlights().isEmpty())
                                        .map(day ->
                                                FlightResult.builder().legs(day.getFlights().stream().map(flight ->
                                                        Flight.builder().departureDateTime(Date.from(LocalDateTime.of(
                                                                calendar.get(Calendar.YEAR),
                                                                calendar.get(Calendar.MONTH) + 1,
                                                                calendar.get(Calendar.DAY_OF_MONTH),
                                                                Integer.parseInt(flight.getDepartureTime().split(":")[0]),
                                                                Integer.parseInt(flight.getDepartureTime().split(":")[1])).atZone(ZoneId.systemDefault()).toInstant()))
                                                                .arrivalDateTime(Date.from(LocalDateTime.of(
                                                                        calendar.get(Calendar.YEAR),
                                                                        calendar.get(Calendar.MONTH) + 1,
                                                                        calendar.get(Calendar.DAY_OF_MONTH),
                                                                        Integer.parseInt(flight.getArrivalTime().split(":")[0]),
                                                                        Integer.parseInt(flight.getArrivalTime().split(":")[1])).atZone(ZoneId.systemDefault()).toInstant()))
                                                                .arrivalAirport(route.getAirportTo())
                                                                .departureAirport(route.getAirportFrom())
                                                                .build()
                                                ).collect(Collectors.toList()))
                                                        .stops(day.getFlights().size() -1)
                                                        .build())
                                        .collect(Collectors.toList())
                        ));
    }

}
