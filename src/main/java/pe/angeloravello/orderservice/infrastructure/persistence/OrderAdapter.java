package pe.angeloravello.orderservice.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import pe.angeloravello.orderservice.application.repository.OrderRepository;
import pe.angeloravello.orderservice.domain.model.Order;
import pe.angeloravello.orderservice.infrastructure.persistence.dto.OrderWithItemsDto;
import pe.angeloravello.orderservice.infrastructure.persistence.entity.OrderEntity;
import pe.angeloravello.orderservice.infrastructure.persistence.entity.OrderItemEntity;
import pe.angeloravello.orderservice.infrastructure.persistence.repository.R2dbcOrderItemRepository;
import pe.angeloravello.orderservice.infrastructure.persistence.repository.R2dbcOrderRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderAdapter implements OrderRepository {

    private final R2dbcOrderRepository orderRepository;
    private final R2dbcOrderItemRepository orderItemRepository;
    private final ConversionService conversionService;



    @Override
    public Mono<Order> save(Order order) {
        var entity = conversionService.convert(order, OrderEntity.class);
        return orderRepository.save(entity)
                .flatMap(savedEntity -> {
                    List<OrderItemEntity> itemEntities = order.getItems().stream()
                            .map(item -> conversionService.convert(item, OrderItemEntity.class))
                            .peek(item -> item.setOrderId(savedEntity.getId()))
                            .toList();
                    return orderItemRepository.saveAll(itemEntities)
                            .collectList()
                            .map(orderItems -> new OrderWithItemsDto(entity, orderItems))
                            .map(orderWithItemsDto -> conversionService.convert(orderWithItemsDto, Order.class));

                });
    }

    @Override
    public Flux<Order> findAll() {
        return orderRepository.findAll()
                .flatMap(entity -> orderItemRepository.findByOrderId(entity.getId())
                        .collectList()
                        .map(items -> new OrderWithItemsDto(entity, items))
                )
                .map(orderWithItemsDto -> conversionService.convert(orderWithItemsDto, Order.class));
    }
}
