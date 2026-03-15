package pe.angeloravello.orderservice.application.usecase.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.angeloravello.orderservice.application.command.CreateOrderCommand;
import pe.angeloravello.orderservice.application.validator.OrderCommandValidator;
import pe.angeloravello.orderservice.application.usecase.CreateOrderUseCase;
import pe.angeloravello.orderservice.application.repository.OrderRepository;
import pe.angeloravello.orderservice.application.repository.ProductRepository;
import pe.angeloravello.orderservice.application.repository.StockReservationRepository;
import pe.angeloravello.orderservice.domain.model.Order;
import pe.angeloravello.orderservice.domain.model.OrderItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CreateOrderUseCaseImpl implements CreateOrderUseCase {

    private final ProductRepository productRepository;
    private final StockReservationRepository stockReservationRepository;
    private final OrderRepository orderRepository;
    private final OrderCommandValidator validator;

    @Override
    public Mono<Order> execute(CreateOrderCommand command) {
        return validator.validate(command)
                .thenMany(Flux.fromIterable(command.items()))
                .filter(item -> item.quantity() != null && item.quantity() > 0)
                .flatMap(item -> productRepository.findById(item.productId())
                        .map(product -> OrderItem.create(
                                product.productId(),
                                product.name(),
                                product.price(),
                                item.quantity())))
                .collectList()
                .flatMap(items -> stockReservationRepository.reserveStock(items).thenReturn(items))
                .map(items -> Order.place(command.customerEmail(), items))
                .flatMap(orderRepository::save);
    }
}