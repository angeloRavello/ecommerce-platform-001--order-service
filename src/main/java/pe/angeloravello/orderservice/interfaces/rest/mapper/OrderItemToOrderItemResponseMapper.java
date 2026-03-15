package pe.angeloravello.orderservice.interfaces.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;
import pe.angeloravello.orderservice.domain.model.OrderItem;
import pe.angeloravello.orderservice.interfaces.rest.response.OrderItemResponse;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface OrderItemToOrderItemResponseMapper extends Converter<OrderItem, OrderItemResponse> {

    @Mapping(target = "unitPrice", expression = "java(source.getUnitPrice().amount())")
    @Mapping(target = "itemPrice", expression = "java(source.subtotal().amount())")
    OrderItemResponse convert(OrderItem source);
}
