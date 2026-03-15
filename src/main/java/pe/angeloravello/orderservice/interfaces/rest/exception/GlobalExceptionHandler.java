package pe.angeloravello.orderservice.interfaces.rest.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.angeloravello.orderservice.domain.exception.InsufficientStockException;
import pe.angeloravello.orderservice.domain.exception.ProductNotFoundException;
import pe.angeloravello.orderservice.domain.exception.OrderDomainException;
import pe.angeloravello.orderservice.interfaces.rest.response.ErrorResponse;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ErrorResponse> handleProductNotFound(ProductNotFoundException ex) {
        return Mono.just(ErrorResponse.of(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ExceptionHandler(InsufficientStockException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Mono<ErrorResponse> handleInsufficientStock(InsufficientStockException ex) {
        return Mono.just(ErrorResponse.of(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage()));
    }

    @ExceptionHandler(OrderDomainException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ErrorResponse> handleOrderDomain(OrderDomainException ex) {
        return Mono.just(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ErrorResponse> handleGeneric(Exception ex) {
        log.error("unexpected:", ex);
        return Mono.just(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred"));
    }
}
