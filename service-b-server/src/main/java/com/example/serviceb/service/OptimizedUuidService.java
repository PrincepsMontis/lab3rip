package com.example.serviceb.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service("optimizedUuidService")
public class OptimizedUuidService implements UuidGenerator {

    // Главное улучшение: переиспользуем RNG (нет new SecureRandom на каждый запрос)
    private static final SecureRandom RNG = new SecureRandom();
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();

    @Override
    public Mono<String> generateOne() {
        return Mono.fromSupplier(this::fastUuid);
    }

    @Override
    public Flux<String> generateMany(int count) {
        return Flux.range(0, Math.max(0, count)).map(i -> fastUuid());
    }

    private String fastUuid() {
        // без String.format: меньше аллокаций
        long ts = Instant.now().toEpochMilli();
        byte[] rnd = new byte[16];
        RNG.nextBytes(rnd);

        // минимально допустимое “преобразование”, можно убрать совсем
        String r = B64.encodeToString(rnd);

        return new StringBuilder(48)
                .append(ts)
                .append('-')
                .append(r)
                .toString();
    }
}
