package com.example.servicea.controller;

import com.example.servicea.client.UuidClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/client/uuid")
@RequiredArgsConstructor
public class UuidController {

    private final UuidClient uuidClient;

    @GetMapping("/single")
    public Mono<String> getSingleUuid() {
        log.info("Клиентский контроллер: запрос одного UUID");
        return uuidClient.getSingleUuid();
    }

    @GetMapping(value = "/batch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getBatchUuids(@RequestParam(defaultValue = "3") int count) {
        log.info("Клиентский контроллер: запрос {} UUID", count);
        return uuidClient.getBatchUuids(count);
    }

    @PostMapping("/custom")
    public Mono<String> getCustomUuid(@RequestBody String seed) {
        log.info("Клиентский контроллер: запрос custom UUID, seed={}", seed);
        return uuidClient.getCustomUuid(seed);
    }
}
