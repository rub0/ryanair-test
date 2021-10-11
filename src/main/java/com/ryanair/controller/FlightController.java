package com.ryanair.controller;

import com.ryanair.entities.FlightResult;
import com.ryanair.services.FlightsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Date;

@Validated
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "")
public class FlightController {

    private final FlightsService service;

    @GetMapping
    public Flux<FlightResult> findFlightsBetween(
            @RequestParam(value = "departure", required = false) String departure,
            @RequestParam(value = "arrival", required = false) String arrival,
            @RequestParam(value = "departureDateTime", required = false) Date departureDateTime,
            @RequestParam(value = "arrivalDateTime", required = false) Date arrivalDateTime
    ){

        return service.findFlightsBetween(departure, arrival, departureDateTime, arrivalDateTime);
    }
}
