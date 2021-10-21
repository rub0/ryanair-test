package com.ryanair.controller;

import com.ryanair.services.FlightsService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureWebTestClient
@Slf4j
public class FlightControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    FlightsService service;

    @Test
    void testOk() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/somevalidcontext/interconnections")
                        .queryParam("departure", "DUB")
                        .queryParam("arrival", "WRO")
                        .queryParam("departureDateTime", "2022-03-01T07:00")
                        .queryParam("arrivalDateTime", "2022-03-03T21:00")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$[0].stops").isEqualTo(1)
                .jsonPath("$[0].legs").isArray();
    }

    @Test
    void testNotFound() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/somevalidcontext/interconnections")
                        .queryParam("departure", "YHJ")
                        .queryParam("arrival", "WRO")
                        .queryParam("departureDateTime", "2022-03-01T07:00")
                        .queryParam("arrivalDateTime", "2022-03-03T21:00")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void testBadRequest() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/somevalidcontext/interconnections")
                        .queryParam("departure", "DUB")
                        .queryParam("arrival", "WRO")
                        .queryParam("arrivalDateTime", "2022-03-03T21:00")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

}
