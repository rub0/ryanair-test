package com.ryanair.controller;

import com.ryanair.entities.FlightResult;
import com.ryanair.exception.NotFoundException;
import com.ryanair.services.FlightsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import java.text.DateFormat;
import java.util.Date;

@Validated
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "")
public class FlightController {

    private final FlightsService service;

    @GetMapping("/{context}/interconnections")
    public Flux<FlightResult> findFlightsBetween(
            @PathVariable String context,
            @RequestParam(value = "departure") String departure,
            @RequestParam(value = "arrival") String arrival,
            @RequestParam(value = "departureDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date departureDateTime,
            @RequestParam(value = "arrivalDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date arrivalDateTime
    ){

        return service.findFlightsBetween(departure, arrival, departureDateTime, arrivalDateTime)
                .switchIfEmpty(Flux.error(new NotFoundException()));
    }
}
