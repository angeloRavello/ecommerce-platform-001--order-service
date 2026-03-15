package pe.angeloravello.orderservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;
import pe.angeloravello.orderservice.domain.model.Order;
import pe.angeloravello.orderservice.infrastructure.persistence.entity.OrderEntity;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface OrderToOrderEntityMapper extends Converter<Order, OrderEntity> {


    @Mapping(target = "totalAmount", source = "source.totalAmount.amount")
    OrderEntity convert(Order source);
}
