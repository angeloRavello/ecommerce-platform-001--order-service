package pe.angeloravello.orderservice.application.validator;

import org.springframework.stereotype.Component;
import pe.angeloravello.orderservice.application.command.CreateOrderCommand;
import pe.angeloravello.orderservice.domain.exception.OrderDomainException;
import reactor.core.publisher.Mono;

@Component
public class OrderCommandValidator {

    public Mono<Void> validate(CreateOrderCommand command) {
        if (command.customerEmail() == null || command.customerEmail().isBlank()) {
            return Mono.error(new OrderDomainException("Customer email is required"));
        }
        if (command.items() == null || command.items().isEmpty()) {
            return Mono.error(new OrderDomainException("Order must contain at least one item"));
        }
        return Mono.empty();
    }
}