package com.ryanair.clients.impl;

import com.ryanair.clients.RyanairAPIClient;
import com.ryanair.entities.Route;
import com.ryanair.entities.Schedule;
import com.ryanair.exception.NoRetryException;
import com.ryanair.exception.RetryException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@Slf4j
public class RyanairAPIClientImpl implements RyanairAPIClient {

    private final WebClient webClient;
    private final Integer maxAttempts;
    private final String findAllRoutesPath;
    private final String findAllflightsFromPath;
    private final Long minBackoff;

    public RyanairAPIClientImpl(WebClient.Builder webClientBuilder,
                                @Value("${daas-events.base-url}") String baseUrl,
                                @Value("${daas-events.base-url}") String findAllRoutesPath,
                                @Value("${daas-events.base-url}") String findAllflightsFromPath,
                                @Value("${daas-events.retry.max-attempts:5}") Integer maxAttempts,
                                @Value("${daas-events.retry.min-backoff-ms:100}") Long minBackoff
    ) {
        this.webClient = webClientBuilder.baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.newConnection().compress(true))).build();
        this.minBackoff = minBackoff;
        this.maxAttempts = maxAttempts;
        this.findAllflightsFromPath = findAllflightsFromPath;
        this.findAllRoutesPath = findAllRoutesPath;
    }

    @Override
    public Flux<Route> findAllRoutes() {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path(findAllRoutesPath)
                        .build())
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, response -> {
                    log.warn("Find all routes not found");
                    return Mono.empty();
                })
                .onStatus(HttpStatus::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(body -> {
                            log.error("Client error in daas-flow-configurator: {}", body);
                            return Mono.error(new NoRetryException());
                        }))
                .bodyToFlux(Route.class)
                .retryWhen(Retry.backoff(this.maxAttempts, Duration.ofMillis(this.minBackoff))
                        .filter(throwable -> throwable instanceof WebClientResponseException &&
                                ((WebClientResponseException) throwable).getStatusCode().is5xxServerError())
                        .onRetryExhaustedThrow((backoff, signal) -> new RetryException()));
    }

    @Override
    public Flux<Schedule> findAllflightsFrom(String from, int year, int month) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path(findAllflightsFromPath)
                        .build())
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, response -> {
                    log.warn("Find all routes not found");
                    return Mono.empty();
                })
                .onStatus(HttpStatus::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(body -> {
                            log.error("Client error in daas-flow-configurator: {}", body);
                            return Mono.error(new NoRetryException());
                        }))
                .bodyToFlux(Schedule.class)
                .retryWhen(Retry.backoff(this.maxAttempts, Duration.ofMillis(this.minBackoff))
                        .filter(throwable -> throwable instanceof WebClientResponseException &&
                                ((WebClientResponseException) throwable).getStatusCode().is5xxServerError())
                        .onRetryExhaustedThrow((backoff, signal) -> new RetryException()));
    }
}
