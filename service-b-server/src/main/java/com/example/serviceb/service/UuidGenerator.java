package com.example.serviceb.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UuidGenerator {
    Mono<String> generateOne();
    Flux<String> generateMany(int count);
}
