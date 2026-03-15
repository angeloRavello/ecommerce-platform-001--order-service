package pe.angeloravello.orderservice.application.usecase;

import pe.angeloravello.orderservice.application.command.CreateOrderCommand;
import pe.angeloravello.orderservice.domain.model.Order;
import reactor.core.publisher.Mono;

public interface CreateOrderUseCase {
    Mono<Order> execute(CreateOrderCommand command);
}
