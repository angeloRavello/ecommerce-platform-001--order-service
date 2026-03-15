package pe.angeloravello.orderservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;
import pe.angeloravello.orderservice.domain.model.Money;
import pe.angeloravello.orderservice.domain.model.OrderItem;
import pe.angeloravello.orderservice.infrastructure.persistence.entity.OrderItemEntity;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface OrderItemEntityToOrderItemMapper extends Converter<OrderItemEntity, OrderItem> {

    default OrderItem convert(OrderItemEntity itemEntity) {
        return OrderItem.reconstitute(
                itemEntity.getId(),
                itemEntity.getProductId(),
                itemEntity.getProductName(),
                Money.of(itemEntity.getUnitPrice()),
                itemEntity.getQuantity()
        );
    }
}
