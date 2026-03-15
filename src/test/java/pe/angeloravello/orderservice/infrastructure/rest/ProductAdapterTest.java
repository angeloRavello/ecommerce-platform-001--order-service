package pe.angeloravello.orderservice.infrastructure.rest;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import pe.angeloravello.orderservice.domain.exception.ProductNotFoundException;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductAdapterTest {

    private MockWebServer mockWebServer;
    private ProductAdapter productAdapter;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        productAdapter = new ProductAdapter(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void findById_returnsProductDto_whenProductExists() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"id": 1, "name": "iPhone 15", "price": 999.99}
                        """));

        StepVerifier.create(productAdapter.findById(1L))
                .assertNext(product -> {
                    assertThat(product.productId()).isEqualTo(1L);
                    assertThat(product.name()).isEqualTo("iPhone 15");
                    assertThat(product.price().amount()).isEqualByComparingTo(BigDecimal.valueOf(999.99));
                })
                .verifyComplete();

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/api/products/1");
    }

    @Test
    void findById_throwsProductNotFoundException_whenProductNotFound() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        StepVerifier.create(productAdapter.findById(99L))
                .expectErrorMatches(ex -> ex instanceof ProductNotFoundException
                        && ex.getMessage().contains("99"))
                .verify();
    }

    @Test
    void findById_propagatesWebClientError_whenServerFails() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        StepVerifier.create(productAdapter.findById(1L))
                .expectErrorMatches(ex ->
                        ex instanceof org.springframework.web.reactive.function.client.WebClientResponseException
                        && ((org.springframework.web.reactive.function.client.WebClientResponseException) ex)
                                .getStatusCode().value() == 500)
                .verify();
    }

    @Test
    void findById_mapsAllFieldsCorrectly() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"id": 42, "name": "MacBook Pro", "price": 2499.00}
                        """));

        StepVerifier.create(productAdapter.findById(42L))
                .assertNext(product -> {
                    assertThat(product.productId()).isEqualTo(42L);
                    assertThat(product.name()).isEqualTo("MacBook Pro");
                    assertThat(product.price().amount()).isEqualByComparingTo(new BigDecimal("2499.00"));
                })
                .verifyComplete();
    }
}
