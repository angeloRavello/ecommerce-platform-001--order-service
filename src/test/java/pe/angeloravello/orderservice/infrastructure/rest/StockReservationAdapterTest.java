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
import pe.angeloravello.orderservice.domain.exception.InsufficientStockException;
import pe.angeloravello.orderservice.domain.model.Money;
import pe.angeloravello.orderservice.domain.model.OrderItem;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StockReservationAdapterTest {

    private MockWebServer mockWebServer;
    private StockReservationAdapter stockReservationAdapter;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        stockReservationAdapter = new StockReservationAdapter(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void reserveStock_completesSuccessfully_whenReservationAccepted() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        StepVerifier.create(stockReservationAdapter.reserveStock(buildItems()))
                .verifyComplete();

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/api/products/reserve");
        assertThat(request.getHeader(HttpHeaders.CONTENT_TYPE))
                .contains(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void reserveStock_sendsCorrectPayload() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        var items = List.of(
                OrderItem.reconstitute(1L, 10L, "iPhone 15", Money.of(BigDecimal.valueOf(999.99)), 2),
                OrderItem.reconstitute(2L, 20L, "MacBook Pro", Money.of(BigDecimal.valueOf(2499.00)), 1)
        );

        StepVerifier.create(stockReservationAdapter.reserveStock(items))
                .verifyComplete();

        String body = mockWebServer.takeRequest().getBody().readUtf8();
        assertThat(body).contains("\"productId\":10", "\"quantity\":2");
        assertThat(body).contains("\"productId\":20", "\"quantity\":1");
    }

    @Test
    void reserveStock_throwsInsufficientStockException_whenServerReturns4xx() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(422));

        StepVerifier.create(stockReservationAdapter.reserveStock(buildItems()))
                .expectErrorMatches(ex -> ex instanceof InsufficientStockException
                        && ex.getMessage().contains("Stock reservation failed"))
                .verify();
    }

    @Test
    void reserveStock_throwsInsufficientStockException_whenServerReturns5xx() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        StepVerifier.create(stockReservationAdapter.reserveStock(buildItems()))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(InsufficientStockException.class);
                    assertThat(ex.getMessage()).contains("Stock reservation failed");
                })
                .verify();
    }

    @Test
    void reserveStock_mapsAllItemsIntoRequest() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        var items = List.of(
                OrderItem.reconstitute(1L, 5L, "Item A", Money.of(BigDecimal.TEN), 3),
                OrderItem.reconstitute(2L, 6L, "Item B", Money.of(BigDecimal.TEN), 7)
        );

        StepVerifier.create(stockReservationAdapter.reserveStock(items))
                .verifyComplete();

        String body = mockWebServer.takeRequest().getBody().readUtf8();
        assertThat(body).contains("\"productId\":5", "\"productId\":6");
        assertThat(body).contains("\"quantity\":3", "\"quantity\":7");
    }

    private List<OrderItem> buildItems() {
        return List.of(
                OrderItem.reconstitute(1L, 10L, "iPhone 15", Money.of(BigDecimal.valueOf(999.99)), 2)
        );
    }
}
