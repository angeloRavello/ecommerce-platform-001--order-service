package pe.angeloravello.orderservice.infrastructure.rest.response;

import java.math.BigDecimal;

public record ProductResponse(Long id, String name, BigDecimal price) {}
