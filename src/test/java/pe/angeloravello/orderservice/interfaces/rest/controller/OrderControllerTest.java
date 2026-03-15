package pe.angeloravello.orderservice.interfaces.rest.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import pe.angeloravello.orderservice.application.command.CreateOrderCommand;
import pe.angeloravello.orderservice.application.dto.OrderItemDto;
import pe.angeloravello.orderservice.domain.exception.ProductNotFoundException;
import pe.angeloravello.orderservice.application.usecase.CreateOrderUseCase;
import pe.angeloravello.orderservice.application.usecase.ListOrdersUseCase;
import pe.angeloravello.orderservice.domain.model.Money;
import pe.angeloravello.orderservice.domain.model.Order;
import pe.angeloravello.orderservice.domain.model.OrderItem;
import pe.angeloravello.orderservice.domain.model.OrderStatus;
import pe.angeloravello.orderservice.interfaces.rest.OrderController;
import pe.angeloravello.orderservice.interfaces.rest.response.OrderItemResponse;
import pe.angeloravello.orderservice.interfaces.rest.response.OrderResponse;
import pe.angeloravello.orderservice.interfaces.rest.exception.GlobalExceptionHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock private CreateOrderUseCase createOrderUseCase;
    @Mock private ListOrdersUseCase listOrdersUseCase;
    @Mock private ConversionService conversionService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        var controller = new OrderController(createOrderUseCase, listOrdersUseCase, conversionService);
        webTestClient = WebTestClient.bindToController(controller)
                .controllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // --- POST /api/orders ---

    @Test
    void createOrder_returnsOk_whenValidRequest() {
        var command = new CreateOrderCommand("customer@example.com",
                List.of(new OrderItemDto(1L, 2)));
        var order = buildOrder(1L);
        var response = buildOrderResponse(1L);

        when(conversionService.convert(any(), eq(CreateOrderCommand.class))).thenReturn(command);
        when(createOrderUseCase.execute(command)).thenReturn(Mono.just(order));
        when(conversionService.convert(any(Order.class), eq(OrderResponse.class))).thenReturn(response);

        webTestClient.post().uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"customerEmail":"customer@example.com","items":[{"productId":1,"quantity":2}]}
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponse.class)
                .value(r -> {
                    assertThat(r.id()).isEqualTo(1L);
                    assertThat(r.customerEmail()).isEqualTo("customer@example.com");
                    assertThat(r.status()).isEqualTo("PENDING");
                });
    }

    @Test
    void createOrder_returns404_whenProductNotFound() {
        var command = new CreateOrderCommand("customer@example.com",
                List.of(new OrderItemDto(99L, 1)));

        when(conversionService.convert(any(), eq(CreateOrderCommand.class))).thenReturn(command);
        when(createOrderUseCase.execute(command))
                .thenReturn(Mono.error(new ProductNotFoundException(99L)));

        webTestClient.post().uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"customerEmail":"customer@example.com","items":[{"productId":99,"quantity":1}]}
                        """)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").value(msg ->
                        assertThat((String) msg).contains("99"));
    }

    // --- GET /api/orders ---

    @Test
    void getAll_returnsOrderList() {
        var order1 = buildOrder(1L);
        var order2 = buildOrder(2L);
        var response1 = buildOrderResponse(1L);
        var response2 = buildOrderResponse(2L);

        when(listOrdersUseCase.execute()).thenReturn(Flux.just(order1, order2));
        when(conversionService.convert(eq(order1), eq(OrderResponse.class))).thenReturn(response1);
        when(conversionService.convert(eq(order2), eq(OrderResponse.class))).thenReturn(response2);

        webTestClient.get().uri("/api/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrderResponse.class)
                .hasSize(2)
                .value(list -> assertThat(list).extracting(OrderResponse::id).containsExactly(1L, 2L));
    }

    @Test
    void getAll_returnsEmptyList_whenNoOrders() {
        when(listOrdersUseCase.execute()).thenReturn(Flux.empty());

        webTestClient.get().uri("/api/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrderResponse.class)
                .hasSize(0);
    }

    // --- helpers ---

    private Order buildOrder(Long id) {
        return Order.reconstitute(id, "customer@example.com",
                Money.of(BigDecimal.valueOf(1999.98)),
                OrderStatus.PENDING, LocalDateTime.now(),
                List.of(OrderItem.reconstitute(1L, 1L, "iPhone 15",
                        Money.of(BigDecimal.valueOf(999.99)), 2)));
    }

    private OrderResponse buildOrderResponse(Long id) {
        return new OrderResponse(id, "customer@example.com",
                BigDecimal.valueOf(1999.98), "PENDING", LocalDateTime.now(),
                List.of(new OrderItemResponse(1L, "iPhone 15", 2, BigDecimal.valueOf(999.99))));
    }
}
