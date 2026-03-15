package pe.angeloravello.orderservice.interfaces.rest.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal itemPrice
) {
    public OrderItemResponse(Long productId, String productName, Integer quantity, BigDecimal unitPrice) {
        this(productId, productName, quantity, unitPrice, unitPrice.multiply(BigDecimal.valueOf(quantity)));
    }
}
