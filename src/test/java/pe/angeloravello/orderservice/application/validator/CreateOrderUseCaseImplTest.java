package pe.angeloravello.orderservice.application.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.angeloravello.orderservice.application.command.CreateOrderCommand;
import pe.angeloravello.orderservice.application.dto.OrderItemDto;
import pe.angeloravello.orderservice.application.dto.ProductDto;
import pe.angeloravello.orderservice.application.repository.OrderRepository;
import pe.angeloravello.orderservice.application.repository.ProductRepository;
import pe.angeloravello.orderservice.application.repository.StockReservationRepository;
import pe.angeloravello.orderservice.application.usecase.impl.CreateOrderUseCaseImpl;
import pe.angeloravello.orderservice.domain.exception.OrderDomainException;
import pe.angeloravello.orderservice.domain.model.Money;
import pe.angeloravello.orderservice.domain.model.Order;
import pe.angeloravello.orderservice.domain.model.OrderItem;
import pe.angeloravello.orderservice.domain.model.OrderStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private StockReservationRepository stockReservationRepository;
    @Mock private OrderRepository orderRepository;

    private CreateOrderUseCaseImpl interactor;

    @BeforeEach
    void setUp() {
        interactor = new CreateOrderUseCaseImpl(
                productRepository, stockReservationRepository, orderRepository,
                new OrderCommandValidator());
    }

    @Test
    void execute_createsOrder_whenValidCommand() {
        var command = new CreateOrderCommand("customer@example.com",
                List.of(new OrderItemDto(1L, 2)));
        var productData = new ProductDto(1L, "iPhone 15", Money.of(BigDecimal.valueOf(999.99)));
        var savedOrder = buildSavedOrder();

        when(productRepository.findById(1L)).thenReturn(Mono.just(productData));
        when(stockReservationRepository.reserveStock(anyList())).thenReturn(Mono.empty());
        when(orderRepository.save(any())).thenReturn(Mono.just(savedOrder));

        StepVerifier.create(interactor.execute(command))
                .assertNext(order -> {
                    assertThat(order.getId()).isEqualTo(1L);
                    assertThat(order.getCustomerEmail()).isEqualTo("customer@example.com");
                    assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
                    assertThat(order.getItems()).hasSize(1);
                    assertThat(order.getTotalAmount().amount())
                            .isEqualByComparingTo(BigDecimal.valueOf(1999.98));
                })
                .verifyComplete();

        verify(productRepository).findById(1L);
        verify(stockReservationRepository).reserveStock(anyList());
        verify(orderRepository).save(any());
    }

    @Test
    void execute_filtersOutZeroQuantityItems() {
        var command = new CreateOrderCommand("customer@example.com",
                List.of(new OrderItemDto(1L, 2), new OrderItemDto(2L, 0)));
        var productData = new ProductDto(1L, "iPhone 15", Money.of(BigDecimal.valueOf(999.99)));
        var savedOrder = buildSavedOrder();

        when(productRepository.findById(1L)).thenReturn(Mono.just(productData));
        when(stockReservationRepository.reserveStock(anyList())).thenReturn(Mono.empty());
        when(orderRepository.save(any())).thenReturn(Mono.just(savedOrder));

        StepVerifier.create(interactor.execute(command))
                .assertNext(order -> assertThat(order.getItems()).hasSize(1))
                .verifyComplete();

        verify(productRepository, times(1)).findById(any());
    }

    @Test
    void execute_returnsError_whenEmailIsBlank() {
        var command = new CreateOrderCommand("  ", List.of(new OrderItemDto(1L, 2)));

        StepVerifier.create(interactor.execute(command))
                .expectErrorMatches(ex -> ex instanceof OrderDomainException
                        && ex.getMessage().contains("email"))
                .verify();

        verifyNoInteractions(productRepository, stockReservationRepository, orderRepository);
    }

    @Test
    void execute_returnsError_whenItemsListIsEmpty() {
        var command = new CreateOrderCommand("customer@example.com", List.of());

        StepVerifier.create(interactor.execute(command))
                .expectErrorMatches(ex -> ex instanceof OrderDomainException
                        && ex.getMessage().contains("item"))
                .verify();

        verifyNoInteractions(productRepository, stockReservationRepository, orderRepository);
    }

    @Test
    void execute_returnsError_whenEmailIsNull() {
        var command = new CreateOrderCommand(null, List.of(new OrderItemDto(1L, 1)));

        StepVerifier.create(interactor.execute(command))
                .expectError(OrderDomainException.class)
                .verify();
    }

    @Test
    void execute_propagatesError_whenProductServiceFails() {
        var command = new CreateOrderCommand("customer@example.com",
                List.of(new OrderItemDto(99L, 1)));

        when(productRepository.findById(99L))
                .thenReturn(Mono.error(new RuntimeException("Product service down")));

        StepVerifier.create(interactor.execute(command))
                .expectError(RuntimeException.class)
                .verify();

        verifyNoInteractions(orderRepository);
    }

    private Order buildSavedOrder() {
        return Order.reconstitute(1L, "customer@example.com",
                Money.of(BigDecimal.valueOf(1999.98)),
                OrderStatus.PENDING, LocalDateTime.now(),
                List.of(OrderItem.reconstitute(1L, 1L, "iPhone 15",
                        Money.of(BigDecimal.valueOf(999.99)), 2)));
    }
}
