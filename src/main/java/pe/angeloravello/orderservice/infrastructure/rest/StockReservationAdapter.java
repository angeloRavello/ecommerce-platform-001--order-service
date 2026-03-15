package pe.angeloravello.orderservice.infrastructure.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pe.angeloravello.orderservice.domain.exception.InsufficientStockException;
import pe.angeloravello.orderservice.application.repository.StockReservationRepository;
import pe.angeloravello.orderservice.domain.model.OrderItem;
import pe.angeloravello.orderservice.infrastructure.rest.dto.ProductReservationDto;
import pe.angeloravello.orderservice.infrastructure.rest.request.ReservationRequest;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StockReservationAdapter implements StockReservationRepository {

    private final WebClient productWebClient;

    @Override
    public Mono<Void> reserveStock(List<OrderItem> items) {
        var body = items.stream()
                .map(r -> new ProductReservationDto(r.getProductId(), r.getQuantity()))
                .toList();
        return productWebClient.post()
                .uri("/api/products/reserve")
                .bodyValue(new ReservationRequest(body))
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorMap(WebClientResponseException.class,
                        ex -> new InsufficientStockException("Stock reservation failed: " + ex.getMessage()));
    }
}