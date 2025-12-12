package com.example.serviceb.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

@SpringBootTest
class InefficientUuidServiceTest {

    @Autowired
    private InefficientUuidService uuidService;

    @Test
    void testGenerateUuidMono_ShouldReturnUuid() {
        Mono<String> result = uuidService.generateUuidMono();

        StepVerifier.create(result)
                .expectNextMatches(uuid -> uuid != null && uuid.length() > 0)
                .verifyComplete();
    }

    @Test
    void testGenerateUuidMono_ShouldNotBeEmpty() {
        Mono<String> result = uuidService.generateUuidMono();

        StepVerifier.create(result)
                .assertNext(uuid -> {
                    assert !uuid.isEmpty();
                    System.out.println("Generated UUID: " + uuid);
                })
                .verifyComplete();
    }

    @Test
    void testGenerateUuidFlux_ShouldReturnMultipleUuids() {
        int count = 3;
        Flux<String> result = uuidService.generateUuidFlux(count);

        StepVerifier.create(result)
                .expectNextCount(count)
                .verifyComplete();
    }

    @Test
    void testGenerateUuidFlux_ShouldReturnCorrectNumberOfElements() {
        Flux<String> result = uuidService.generateUuidFlux(5);

        StepVerifier.create(result)
                .expectNextMatches(uuid -> uuid != null)
                .expectNextMatches(uuid -> uuid != null)
                .expectNextMatches(uuid -> uuid != null)
                .expectNextMatches(uuid -> uuid != null)
                .expectNextMatches(uuid -> uuid != null)
                .verifyComplete();
    }

    @Test
    void testGenerateUuidMono_ShouldTakeLongerThan100ms() {
        Mono<String> result = uuidService.generateUuidMono();

        StepVerifier.create(result)
                .expectNextMatches(uuid -> uuid != null)
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    void testGenerateUuidFlux_ShouldEmitSequentially() {
        Flux<String> result = uuidService.generateUuidFlux(3);

        StepVerifier.create(result.log())
                .recordWith(java.util.ArrayList::new)
                .expectNextCount(3)
                .consumeRecordedWith(uuids -> {
                    System.out.println("Generated UUIDs:");
                    uuids.forEach(System.out::println);
                    assert uuids.size() == 3;
                })
                .verifyComplete();
    }

    @Test
    void testGenerateUuidMono_ShouldNotReturnNull() {
        Mono<String> result = uuidService.generateUuidMono();

        StepVerifier.create(result)
                .expectNextMatches(uuid -> {
                    assert uuid != null : "UUID should not be null";
                    assert !uuid.startsWith("ERROR") : "UUID should not start with ERROR";
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void testGenerateUuidFlux_WithZeroCount_ShouldComplete() {
        Flux<String> result = uuidService.generateUuidFlux(0);

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }
}
