package pe.angeloravello.orderservice.interfaces.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;
import pe.angeloravello.orderservice.domain.model.Order;
import pe.angeloravello.orderservice.domain.model.OrderItem;
import pe.angeloravello.orderservice.interfaces.rest.response.OrderItemResponse;
import pe.angeloravello.orderservice.interfaces.rest.response.OrderResponse;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, uses = OrderItemToOrderItemResponseMapper.class)
public interface OrderToOrderResponseMapper extends Converter<Order, OrderResponse> {

    @Mapping(target = "totalAmount", expression = "java(source.getTotalAmount().amount())")
    @Mapping(target = "status", expression = "java(source.getStatus().name())")
    OrderResponse convert(Order source);


}
