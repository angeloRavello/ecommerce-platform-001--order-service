package pe.angeloravello.orderservice.domain.exception;

public class OrderDomainException extends RuntimeException {
    public OrderDomainException(String message) {
        super(message);
    }
}
