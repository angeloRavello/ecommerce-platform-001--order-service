package pe.angeloravello.orderservice.infrastructure.persistence.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import pe.angeloravello.orderservice.infrastructure.persistence.entity.OrderItemEntity;
import reactor.core.publisher.Flux;

public interface R2dbcOrderItemRepository extends ReactiveCrudRepository<OrderItemEntity, Long> {
    Flux<OrderItemEntity> findByOrderId(Long orderId);
}
