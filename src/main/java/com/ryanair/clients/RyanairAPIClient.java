package com.ryanair.clients;

import com.ryanair.entities.Route;
import com.ryanair.entities.Schedule;
import reactor.core.publisher.Flux;

public interface RyanairAPIClient {
    Flux<Route> findAllRoutes();
    Flux<Schedule> findAllflightsFrom(String from, int year, int month);
}
