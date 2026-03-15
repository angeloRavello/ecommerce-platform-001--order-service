package pe.angeloravello.orderservice.application.command;

import pe.angeloravello.orderservice.application.dto.OrderItemDto;

import java.util.List;

public record CreateOrderCommand(
        String customerEmail,
        List<OrderItemDto> items
) {}
