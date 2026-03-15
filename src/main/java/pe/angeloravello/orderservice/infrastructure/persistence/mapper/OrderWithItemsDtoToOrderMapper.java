package pe.angeloravello.orderservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import pe.angeloravello.orderservice.domain.model.Money;
import pe.angeloravello.orderservice.domain.model.Order;
import pe.angeloravello.orderservice.domain.model.OrderStatus;
import pe.angeloravello.orderservice.infrastructure.persistence.dto.OrderWithItemsDto;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public abstract class OrderWithItemsDtoToOrderMapper implements Converter<OrderWithItemsDto, Order> {

    @Autowired
    private OrderItemEntityToOrderItemMapper itemMapper;

    @Nullable
    @Override
    public Order convert(OrderWithItemsDto source) {
        return Order.reconstitute(
                source.order().getId(),
                source.order().getCustomerEmail(),
                Money.of(source.order().getTotalAmount()),
                OrderStatus.valueOf(source.order().getStatus()),
                source.order().getCreatedAt(),
                source.orderItems().stream().map(itemMapper::convert).toList()
        );
    }

}
