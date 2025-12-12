package com.example.serviceb.controller;

import com.example.serviceb.service.InefficientUuidService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@WebFluxTest(UuidGeneratorController.class)
class UuidGeneratorControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private InefficientUuidService uuidService;

    @Test
    void testGenerateSingleUuid_ShouldReturn200() {
        String mockUuid = "test-uuid-12345";
        when(uuidService.generateUuidMono()).thenReturn(Mono.just(mockUuid));

        webTestClient.get()
                .uri("/api/uuid/single")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(mockUuid);
    }

    @Test
    void testGenerateBatchUuid_ShouldReturnFlux() {
        Flux<String> mockUuids = Flux.just("uuid-1", "uuid-2", "uuid-3");
        when(uuidService.generateUuidFlux(anyInt())).thenReturn(mockUuids);

        webTestClient.get()
                .uri("/api/uuid/batch?count=3")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(String.class)
                .hasSize(3);
    }

    @Test
    void testGenerateCustomUuid_ShouldAcceptPostRequest() {
        String mockUuid = "custom-uuid-67890";
        when(uuidService.generateUuidMono()).thenReturn(Mono.just(mockUuid));

        webTestClient.post()
                .uri("/api/uuid/custom")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue("my-seed")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(mockUuid);
    }

    @Test
    void testGenerateBatchUuid_WithDefaultCount() {
        Flux<String> mockUuids = Flux.just("uuid-1", "uuid-2", "uuid-3");
        when(uuidService.generateUuidFlux(3)).thenReturn(mockUuids);

        webTestClient.get()
                .uri("/api/uuid/batch")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(String.class)
                .hasSize(3);
    }
}
