package pe.angeloravello.orderservice.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
@Getter
public class Order {

    private final Long id;
    private final String customerEmail;
    private final Money totalAmount;
    private final OrderStatus status;
    private final LocalDateTime createdAt;
    private final List<OrderItem> items;


    /** Creates a new order — enforces invariants and computes the total. */
    public static Order place(String customerEmail, List<OrderItem> items) {
        if (items == null || items.isEmpty())
            throw new IllegalArgumentException("An order must have at least one item");
        Money total = items.stream().map(OrderItem::subtotal).reduce(Money.ZERO, Money::add);
        return new Order(null, customerEmail, total,
                OrderStatus.PENDING, LocalDateTime.now(), List.copyOf(items));
    }

    /** Rehydrates an order from persistence — skips business invariants. */
    public static Order reconstitute(Long id, String customerEmail, Money totalAmount,
                                     OrderStatus status, LocalDateTime createdAt, List<OrderItem> items) {
        return new Order(id, customerEmail, totalAmount, status, createdAt, items);
    }
}
