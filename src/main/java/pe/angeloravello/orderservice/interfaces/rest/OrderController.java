package pe.angeloravello.orderservice.interfaces.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.angeloravello.orderservice.application.command.CreateOrderCommand;
import pe.angeloravello.orderservice.application.usecase.CreateOrderUseCase;
import pe.angeloravello.orderservice.application.usecase.ListOrdersUseCase;
import pe.angeloravello.orderservice.interfaces.rest.response.OrderResponse;
import pe.angeloravello.orderservice.interfaces.rest.request.OrderRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final ListOrdersUseCase listOrdersUseCase;
    private final ConversionService conversionService;

    @PostMapping
    public Mono<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        CreateOrderCommand command = conversionService.convert(request, CreateOrderCommand.class);
        log.info("command={}", command);
        return createOrderUseCase.execute(command)
                .mapNotNull(order -> conversionService.convert(order, OrderResponse.class));
    }

    @GetMapping
    public Flux<OrderResponse> getAll() {
        return listOrdersUseCase.execute()
                .mapNotNull(order -> conversionService.convert(order, OrderResponse.class));
    }
}
