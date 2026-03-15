package pe.angeloravello.orderservice.infrastructure.persistence.dto;

import pe.angeloravello.orderservice.infrastructure.persistence.entity.OrderEntity;
import pe.angeloravello.orderservice.infrastructure.persistence.entity.OrderItemEntity;

import java.util.List;

public record OrderWithItemsDto(
        OrderEntity order,
        List<OrderItemEntity> orderItems
) {
}
