package com.example.servicea.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class UuidClient {

    private final WebClient webClient;

    public Mono<String> getSingleUuid() {
        return webClient.get()
                .uri("/api/uuid/single")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(15))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .doBeforeRetry(rs ->
                                log.warn("Повторная попытка запроса UUID, попытка {}", rs.totalRetries() + 1)
                        )
                )
                .doOnSuccess(uuid -> log.info("Получен UUID: {}", uuid))
                .doOnError(e -> log.error("Ошибка при получении UUID: {}", e.getMessage()));
    }

    public Flux<String> getBatchUuids(int count) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/uuid/batch")
                        .queryParam("count", count)
                        .build())
                .retrieve()
                .bodyToFlux(String.class)
                .timeout(Duration.ofSeconds(30))
                .doOnNext(uuid -> log.info("Элемент потока UUID: {}", uuid))
                .doOnComplete(() -> log.info("Поток UUID завершен"))
                .doOnError(e -> log.error("Ошибка в потоке UUID: {}", e.getMessage()));
    }

    public Mono<String> getCustomUuid(String seed) {
        return webClient.post()
                .uri("/api/uuid/custom")
                .bodyValue(seed)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(15))
                .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(2)))
                .doOnSuccess(uuid -> log.info("Получен custom UUID: {}", uuid));
    }
}
