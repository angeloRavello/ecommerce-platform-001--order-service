package pe.angeloravello.orderservice.infrastructure.persistence.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import pe.angeloravello.orderservice.infrastructure.persistence.entity.OrderEntity;

public interface R2dbcOrderRepository extends ReactiveCrudRepository<OrderEntity, Long> {}
