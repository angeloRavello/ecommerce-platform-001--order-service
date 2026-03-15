package pe.angeloravello.orderservice.application.repository;

import pe.angeloravello.orderservice.domain.model.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository {
    Mono<Order> save(Order order);
    Flux<Order> findAll();
}
