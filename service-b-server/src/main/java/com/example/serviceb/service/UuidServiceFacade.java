package com.example.serviceb.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UuidServiceFacade {

    private final UuidGenerator inefficient;
    private final UuidGenerator optimized;

    public UuidServiceFacade(
            @Qualifier("inefficientUuidService") UuidGenerator inefficient,
            @Qualifier("optimizedUuidService") UuidGenerator optimized
    ) {
        this.inefficient = inefficient;
        this.optimized = optimized;
    }

    public Mono<String> one(String mode) {
        return pick(mode).generateOne();
    }

    public Flux<String> many(String mode, int count) {
        return pick(mode).generateMany(count);
    }

    private UuidGenerator pick(String mode) {
        return "optimized".equalsIgnoreCase(mode) ? optimized : inefficient;
    }
}
