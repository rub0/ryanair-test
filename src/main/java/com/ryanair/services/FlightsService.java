package com.ryanair.services;

import com.ryanair.clients.RyanairAPIClient;
import com.ryanair.entities.FlightResult;
import reactor.core.publisher.Flux;

import java.util.Date;

public interface FlightsService {

    Flux<FlightResult> findFlightsBetween(String departure, String arrival, Date departureDateTime, Date arrivalDateTime);
}
