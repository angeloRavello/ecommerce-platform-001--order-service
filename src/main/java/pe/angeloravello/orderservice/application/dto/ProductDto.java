package pe.angeloravello.orderservice.application.dto;

import pe.angeloravello.orderservice.domain.model.Money;

public record ProductDto(Long productId, String name, Money price) {}
