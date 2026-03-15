package pe.angeloravello.orderservice.infrastructure.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pe.angeloravello.orderservice.application.dto.ProductDto;
import pe.angeloravello.orderservice.domain.exception.ProductNotFoundException;
import pe.angeloravello.orderservice.application.repository.ProductRepository;
import pe.angeloravello.orderservice.domain.model.Money;
import pe.angeloravello.orderservice.infrastructure.rest.response.ProductResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductAdapter implements ProductRepository {

    private final WebClient productWebClient;

    @Override
    public Mono<ProductDto> findById(Long productId) {
        return productWebClient.get()
                .uri("/api/products/{id}", productId)
                .retrieve()
                .bodyToMono(ProductResponse.class)
                .map(r -> new ProductDto(r.id(), r.name(), Money.of(r.price())))
                .onErrorMap(WebClientResponseException.NotFound.class,
                        ex -> new ProductNotFoundException(productId));
    }
}