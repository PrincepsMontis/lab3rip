package com.example.serviceb.controller;

import com.example.serviceb.service.UuidServiceFacade;
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

    private final UuidServiceFacade facade;

    // GET /api/uuid/single?mode=inefficient|optimized
    @GetMapping("/single")
    public Mono<String> generateSingleUuid(
            @RequestParam(defaultValue = "inefficient") String mode
    ) {
        log.info("Запрос на генерацию одного UUID, mode={}", mode);
        return facade.one(mode)
                .doOnSuccess(uuid -> log.info("Отправлен UUID (mode={}): {}", mode, uuid));
    }

    // GET /api/uuid/batch?count=5&mode=inefficient|optimized
    @GetMapping(value = "/batch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateBatchUuid(
            @RequestParam(defaultValue = "3") int count,
            @RequestParam(defaultValue = "inefficient") String mode
    ) {
        log.info("Запрос на генерацию {} UUID, mode={}", count, mode);
        return facade.many(mode, count)
                .doOnComplete(() -> log.info("Все UUID отправлены, mode={}", mode));
    }

    // POST /api/uuid/custom?mode=...
    // seed оставляем для совместимости, но генерация зависит от mode
    @PostMapping("/custom")
    public Mono<String> generateCustomUuid(
            @RequestBody String seed,
            @RequestParam(defaultValue = "inefficient") String mode
    ) {
        log.info("Запрос с seed='{}', mode={}", seed, mode);
        return facade.one(mode)
                .doOnSuccess(uuid -> log.info("Отправлен custom UUID (mode={}): {}", mode, uuid));
    }
}
