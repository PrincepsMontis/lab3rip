package com.example.servicea.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

class UuidClientTest {

    private MockWebServer mockWebServer;
    private UuidClient uuidClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        uuidClient = new UuidClient(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testGetSingleUuid_ShouldReturnUuid() {
        String mockUuid = "test-uuid-12345";
        mockWebServer.enqueue(new MockResponse()
                .setBody(mockUuid)
                .addHeader("Content-Type", "text/plain"));

        Mono<String> result = uuidClient.getSingleUuid();

        StepVerifier.create(result)
                .expectNext(mockUuid)
                .verifyComplete();
    }

    @Test
    void testGetSingleUuid_ShouldNotBeEmpty() {
        String mockUuid = "another-uuid-67890";
        mockWebServer.enqueue(new MockResponse()
                .setBody(mockUuid)
                .addHeader("Content-Type", "text/plain"));

        Mono<String> result = uuidClient.getSingleUuid();

        StepVerifier.create(result)
                .assertNext(uuid -> {
                    assert !uuid.isEmpty();
                    assert uuid.equals(mockUuid);
                })
                .verifyComplete();
    }

    @Test
    void testGetCustomUuid_ShouldSendPostRequest() {
        String mockUuid = "custom-uuid-67890";
        mockWebServer.enqueue(new MockResponse()
                .setBody(mockUuid)
                .addHeader("Content-Type", "text/plain"));

        Mono<String> result = uuidClient.getCustomUuid("test-seed");

        StepVerifier.create(result)
                .expectNext(mockUuid)
                .verifyComplete();
    }

    @Test
    void testGetSingleUuid_WithError_ShouldRetry() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse()
                .setBody("uuid-after-retry")
                .addHeader("Content-Type", "text/plain"));

        Mono<String> result = uuidClient.getSingleUuid();

        StepVerifier.create(result)
                .expectNext("uuid-after-retry")
                .verifyComplete();
    }

    @Test
    void testGetSingleUuid_WithTimeout_ShouldFail() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("delayed-uuid")
                .setBodyDelay(20, java.util.concurrent.TimeUnit.SECONDS));

        Mono<String> result = uuidClient.getSingleUuid();

        StepVerifier.create(result)
                .expectError()
                .verify();
    }
}
