package pe.angeloravello.orderservice.infrastructure.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import pe.angeloravello.orderservice.domain.model.Money;
import pe.angeloravello.orderservice.domain.model.Order;
import pe.angeloravello.orderservice.domain.model.OrderItem;
import pe.angeloravello.orderservice.domain.model.OrderStatus;
import pe.angeloravello.orderservice.infrastructure.persistence.dto.OrderWithItemsDto;
import pe.angeloravello.orderservice.infrastructure.persistence.entity.OrderEntity;
import pe.angeloravello.orderservice.infrastructure.persistence.entity.OrderItemEntity;
import pe.angeloravello.orderservice.infrastructure.persistence.repository.R2dbcOrderItemRepository;
import pe.angeloravello.orderservice.infrastructure.persistence.repository.R2dbcOrderRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderAdapterTest {

    @Mock private R2dbcOrderRepository orderRepository;
    @Mock private R2dbcOrderItemRepository orderItemRepository;
    @Mock private ConversionService conversionService;

    private OrderAdapter orderAdapter;

    @BeforeEach
    void setUp() {
        orderAdapter = new OrderAdapter(orderRepository, orderItemRepository, conversionService);
    }

    // --- save ---

    @Test
    void save_persistsOrderAndItems_andReturnsReconstitutedDomain() {
        var order = buildOrder();
        var orderEntity = buildOrderEntity(null);
        var savedOrderEntity = buildOrderEntity(1L);
        var itemEntity = buildOrderItemEntity(null, null);
        var reconstitutedOrder = buildSavedOrder();

        when(conversionService.convert(order, OrderEntity.class)).thenReturn(orderEntity);
        when(orderRepository.save(orderEntity)).thenReturn(Mono.just(savedOrderEntity));
        when(conversionService.convert(any(OrderItem.class), eq(OrderItemEntity.class))).thenReturn(itemEntity);
        when(orderItemRepository.saveAll(anyList())).thenReturn(Flux.just(itemEntity));
        when(conversionService.convert(any(OrderWithItemsDto.class), eq(Order.class))).thenReturn(reconstitutedOrder);

        StepVerifier.create(orderAdapter.save(order))
                .assertNext(result -> {
                    assertThat(result.getId()).isEqualTo(1L);
                    assertThat(result.getCustomerEmail()).isEqualTo("customer@example.com");
                    assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
                    assertThat(result.getItems()).hasSize(1);
                })
                .verifyComplete();

        verify(orderRepository).save(orderEntity);
        verify(orderItemRepository).saveAll(anyList());
    }

    @Test
    void save_setsOrderIdOnItems_beforePersisting() {
        var order = buildOrder();
        var orderEntity = buildOrderEntity(null);
        var savedOrderEntity = buildOrderEntity(42L);
        var itemEntity = buildOrderItemEntity(null, null);
        var reconstitutedOrder = buildSavedOrder();

        when(conversionService.convert(order, OrderEntity.class)).thenReturn(orderEntity);
        when(orderRepository.save(orderEntity)).thenReturn(Mono.just(savedOrderEntity));
        when(conversionService.convert(any(OrderItem.class), eq(OrderItemEntity.class))).thenReturn(itemEntity);
        when(orderItemRepository.saveAll(anyList())).thenReturn(Flux.just(itemEntity));
        when(conversionService.convert(any(OrderWithItemsDto.class), eq(Order.class))).thenReturn(reconstitutedOrder);

        StepVerifier.create(orderAdapter.save(order))
                .assertNext(result -> assertThat(result).isNotNull())
                .verifyComplete();

        assertThat(itemEntity.getOrderId()).isEqualTo(42L);
    }

    @Test
    void save_propagatesError_whenOrderRepositoryFails() {
        var order = buildOrder();
        var orderEntity = buildOrderEntity(null);

        when(conversionService.convert(order, OrderEntity.class)).thenReturn(orderEntity);
        when(orderRepository.save(orderEntity)).thenReturn(Mono.error(new RuntimeException("DB unavailable")));

        StepVerifier.create(orderAdapter.save(order))
                .expectErrorMatches(ex -> ex instanceof RuntimeException
                        && ex.getMessage().equals("DB unavailable"))
                .verify();

        verify(orderRepository).save(orderEntity);
    }

    @Test
    void save_propagatesError_whenItemRepositoryFails() {
        var order = buildOrder();
        var orderEntity = buildOrderEntity(null);
        var savedOrderEntity = buildOrderEntity(1L);
        var itemEntity = buildOrderItemEntity(null, null);

        when(conversionService.convert(order, OrderEntity.class)).thenReturn(orderEntity);
        when(orderRepository.save(orderEntity)).thenReturn(Mono.just(savedOrderEntity));
        when(conversionService.convert(any(OrderItem.class), eq(OrderItemEntity.class))).thenReturn(itemEntity);
        when(orderItemRepository.saveAll(anyList())).thenReturn(Flux.error(new RuntimeException("Item save failed")));

        StepVerifier.create(orderAdapter.save(order))
                .expectError(RuntimeException.class)
                .verify();
    }

    // --- findAll ---

    @Test
    void findAll_returnsAllOrders_withTheirItems() {
        var orderEntity = buildOrderEntity(1L);
        var itemEntity = buildOrderItemEntity(10L, 1L);
        var reconstitutedOrder = buildSavedOrder();

        when(orderRepository.findAll()).thenReturn(Flux.just(orderEntity));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Flux.just(itemEntity));
        when(conversionService.convert(any(OrderWithItemsDto.class), eq(Order.class))).thenReturn(reconstitutedOrder);

        StepVerifier.create(orderAdapter.findAll())
                .assertNext(result -> {
                    assertThat(result.getId()).isEqualTo(1L);
                    assertThat(result.getCustomerEmail()).isEqualTo("customer@example.com");
                    assertThat(result.getItems()).hasSize(1);
                })
                .verifyComplete();

        verify(orderRepository).findAll();
        verify(orderItemRepository).findByOrderId(1L);
    }

    @Test
    void findAll_returnsEmpty_whenNoOrdersExist() {
        when(orderRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(orderAdapter.findAll())
                .verifyComplete();

        verify(orderRepository).findAll();
    }

    @Test
    void findAll_propagatesError_whenOrderRepositoryFails() {
        when(orderRepository.findAll()).thenReturn(Flux.error(new RuntimeException("DB unavailable")));

        StepVerifier.create(orderAdapter.findAll())
                .expectError(RuntimeException.class)
                .verify();
    }

    // --- builders ---

    private Order buildOrder() {
        return Order.reconstitute(null, "customer@example.com",
                Money.of(BigDecimal.valueOf(999.99)),
                OrderStatus.PENDING, LocalDateTime.now(),
                List.of(OrderItem.reconstitute(null, 1L, "iPhone 15",
                        Money.of(BigDecimal.valueOf(999.99)), 1)));
    }

    private Order buildSavedOrder() {
        return Order.reconstitute(1L, "customer@example.com",
                Money.of(BigDecimal.valueOf(999.99)),
                OrderStatus.PENDING, LocalDateTime.now(),
                List.of(OrderItem.reconstitute(10L, 1L, "iPhone 15",
                        Money.of(BigDecimal.valueOf(999.99)), 1)));
    }

    private OrderEntity buildOrderEntity(Long id) {
        return OrderEntity.builder()
                .id(id)
                .customerEmail("customer@example.com")
                .totalAmount(BigDecimal.valueOf(999.99))
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private OrderItemEntity buildOrderItemEntity(Long id, Long orderId) {
        return OrderItemEntity.builder()
                .id(id)
                .orderId(orderId)
                .productId(1L)
                .productName("iPhone 15")
                .unitPrice(BigDecimal.valueOf(999.99))
                .quantity(1)
                .build();
    }
}
