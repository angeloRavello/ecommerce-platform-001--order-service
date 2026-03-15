package pe.angeloravello.orderservice.interfaces.rest.request;

import java.util.List;

public record OrderRequest(
        String customerEmail,
        List<OrderItemRequest> items
) {}
