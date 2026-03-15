package pe.angeloravello.orderservice.interfaces.rest.request;

public record OrderItemRequest(Long productId, Integer quantity) {}
