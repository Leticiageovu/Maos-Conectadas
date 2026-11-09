package com.maosconectadas.backend.order;

import java.time.LocalDate;

public record OrderResponse(
    Long id,
    Long clientId,
    String clientName,
    Long productId,
    String productName,
    Integer quantity,
    LocalDate expectedDate,
    String notes,
    OrderStatus status
) {
    static OrderResponse from(CustomerOrder order) {
        return new OrderResponse(
            order.getId(),
            order.getClient().getId(),
            order.getClient().getName(),
            order.getProduct().getId(),
            order.getProduct().getName(),
            order.getQuantity(),
            order.getExpectedDate(),
            order.getNotes(),
            order.getStatus());
    }
}
