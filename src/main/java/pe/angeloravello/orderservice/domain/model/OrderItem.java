package pe.angeloravello.orderservice.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
@Getter
public class OrderItem {

    private final Long id;
    private final Long productId;
    private final String productName;
    private final Money unitPrice;
    private final int quantity;
    private final Money itemPrice;


    public static OrderItem create(Long productId, String productName, Money unitPrice, int quantity) {
        return new OrderItem(null, productId, productName, unitPrice, quantity, unitPrice.multiply(quantity));
    }

    public static OrderItem reconstitute(Long id, Long productId, String productName, Money unitPrice, int quantity) {
        return new OrderItem(id, productId, productName, unitPrice, quantity, unitPrice.multiply(quantity));
    }

    public Money subtotal() {
        return itemPrice;
    }
}
