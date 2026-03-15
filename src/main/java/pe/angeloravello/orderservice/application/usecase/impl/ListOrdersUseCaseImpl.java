package pe.angeloravello.orderservice.application.usecase.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.angeloravello.orderservice.application.repository.OrderRepository;
import pe.angeloravello.orderservice.application.usecase.ListOrdersUseCase;
import pe.angeloravello.orderservice.domain.model.Order;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class ListOrdersUseCaseImpl implements ListOrdersUseCase {

    private final OrderRepository orderRepository;

    @Override
    public Flux<Order> execute() {
        return orderRepository.findAll();
    }
}