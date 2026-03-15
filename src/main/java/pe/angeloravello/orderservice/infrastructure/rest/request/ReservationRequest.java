package pe.angeloravello.orderservice.infrastructure.rest.request;


import pe.angeloravello.orderservice.infrastructure.rest.dto.ProductReservationDto;

import java.util.List;

public record ReservationRequest(List<ProductReservationDto> reservations) {
}
