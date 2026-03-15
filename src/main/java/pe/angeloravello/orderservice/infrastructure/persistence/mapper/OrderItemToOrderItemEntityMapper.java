package pe.angeloravello.orderservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;
import pe.angeloravello.orderservice.domain.model.Money;
import pe.angeloravello.orderservice.domain.model.OrderItem;
import pe.angeloravello.orderservice.infrastructure.persistence.entity.OrderItemEntity;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface OrderItemToOrderItemEntityMapper extends Converter<OrderItem, OrderItemEntity> {

    @Mapping(target = "unitPrice", source = "source.unitPrice.amount")
    OrderItemEntity convert(OrderItem source);

}
