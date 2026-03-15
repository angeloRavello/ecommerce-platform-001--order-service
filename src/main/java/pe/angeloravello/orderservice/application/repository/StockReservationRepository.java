package pe.angeloravello.orderservice.application.repository;

import pe.angeloravello.orderservice.domain.model.OrderItem;
import reactor.core.publisher.Mono;

import java.util.List;

public interface StockReservationRepository {
    Mono<Void> reserveStock(List<OrderItem> items);
}