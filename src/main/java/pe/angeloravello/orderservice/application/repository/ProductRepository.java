package pe.angeloravello.orderservice.application.repository;

import pe.angeloravello.orderservice.application.dto.ProductDto;
import reactor.core.publisher.Mono;

public interface ProductRepository {
    Mono<ProductDto> findById(Long productId);
}