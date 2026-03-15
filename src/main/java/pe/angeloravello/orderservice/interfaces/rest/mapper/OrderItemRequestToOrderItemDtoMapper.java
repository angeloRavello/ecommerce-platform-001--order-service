package pe.angeloravello.orderservice.interfaces.rest.mapper;

import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;
import pe.angeloravello.orderservice.application.dto.OrderItemDto;
import pe.angeloravello.orderservice.interfaces.rest.request.OrderItemRequest;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface OrderItemRequestToOrderItemDtoMapper extends Converter<OrderItemRequest, OrderItemDto> {
}
