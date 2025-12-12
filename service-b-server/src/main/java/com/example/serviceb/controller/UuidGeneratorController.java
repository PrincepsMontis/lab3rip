package com.example.serviceb.controller;

import com.example.serviceb.service.InefficientUuidService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/uuid")
@RequiredArgsConstructor
public class UuidGeneratorController {

    private final InefficientUuidService uuidService;

    // GET /api/uuid/single — один UUID (Mono)
    @GetMapping("/single")
    public Mono<String> generateSingleUuid() {
        log.info("Запрос на генерацию одного UUID");
        return uuidService.generateUuidMono()
                .doOnSuccess(uuid -> log.info("Отправлен UUID: {}", uuid));
    }

    // GET /api/uuid/batch?count=5 — несколько UUID (Flux)
    @GetMapping(value = "/batch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateBatchUuid(@RequestParam(defaultValue = "3") int count) {
        log.info("Запрос на генерацию {} UUID", count);
        return uuidService.generateUuidFlux(count)
                .doOnComplete(() -> log.info("Все UUID отправлены"));
    }

    // POST /api/uuid/custom — пример POST, логика генерации та же
    @PostMapping("/custom")
    public Mono<String> generateCustomUuid(@RequestBody String seed) {
        log.info("Запрос с seed: {}", seed);
        return uuidService.generateUuidMono();
    }
}
