package com.ryanair.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureWebTestClient
@Slf4j
public class FlightsServiceTest {
    @Autowired
    FlightsService service;

    @Test
    void testOKHappyPath(){
        StepVerifier.create(service.findFlightsBetween("AGP", "WRO",
                Date.from(LocalDateTime.parse("2021-12-03T05:00").atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(LocalDateTime.parse("2021-12-04T16:00").atZone(ZoneId.systemDefault()).toInstant())))
                .assertNext(result -> Assertions.assertTrue(result.getStops() == 1 && result.getLegs().size() == 2))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void testIATANotFound(){
        StepVerifier.create(service.findFlightsBetween("FGY", "WRO",
                Date.from(LocalDateTime.parse("2021-12-03T05:00").atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(LocalDateTime.parse("2021-12-04T16:00").atZone(ZoneId.systemDefault()).toInstant())))
                .expectSubscription()
                .verifyComplete();
    }
}