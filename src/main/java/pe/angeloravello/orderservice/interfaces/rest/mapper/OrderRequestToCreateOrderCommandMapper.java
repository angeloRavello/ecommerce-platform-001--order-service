package pe.angeloravello.orderservice.interfaces.rest.mapper;

import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;
import pe.angeloravello.orderservice.application.command.CreateOrderCommand;
import pe.angeloravello.orderservice.interfaces.rest.request.OrderRequest;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, uses = OrderItemRequestToOrderItemDtoMapper.class)
public interface OrderRequestToCreateOrderCommandMapper extends Converter<OrderRequest, CreateOrderCommand> {

}
