package pe.angeloravello.orderservice.application.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.angeloravello.orderservice.application.repository.OrderRepository;
import pe.angeloravello.orderservice.application.usecase.impl.ListOrdersUseCaseImpl;
import pe.angeloravello.orderservice.domain.model.Money;
import pe.angeloravello.orderservice.domain.model.Order;
import pe.angeloravello.orderservice.domain.model.OrderStatus;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListOrdersUseCaseImplTest {

    @Mock private OrderRepository orderRepository;

    @InjectMocks
    private ListOrdersUseCaseImpl interactor;

    @Test
    void execute_returnsAllOrders() {
        var order1 = Order.reconstitute(1L, "customer@example.com",
                Money.of(BigDecimal.valueOf(1999.98)), OrderStatus.PENDING,
                LocalDateTime.now(), List.of());
        var order2 = Order.reconstitute(2L, "other@example.com",
                Money.of(BigDecimal.valueOf(50)), OrderStatus.CONFIRMED,
                LocalDateTime.now(), List.of());

        when(orderRepository.findAll()).thenReturn(Flux.just(order1, order2));

        StepVerifier.create(interactor.execute())
                .assertNext(o -> assertThat(o.getId()).isEqualTo(1L))
                .assertNext(o -> assertThat(o.getId()).isEqualTo(2L))
                .verifyComplete();
    }

    @Test
    void execute_returnsEmpty_whenNoOrders() {
        when(orderRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(interactor.execute())
                .verifyComplete();
    }
}
