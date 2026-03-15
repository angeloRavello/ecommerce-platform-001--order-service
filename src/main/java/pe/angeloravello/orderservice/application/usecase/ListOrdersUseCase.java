package pe.angeloravello.orderservice.application.usecase;

import pe.angeloravello.orderservice.domain.model.Order;
import reactor.core.publisher.Flux;

public interface ListOrdersUseCase {
    Flux<Order> execute();
}
