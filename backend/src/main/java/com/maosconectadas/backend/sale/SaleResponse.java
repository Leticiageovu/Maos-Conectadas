package com.maosconectadas.backend.sale;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SaleResponse(
    Long id,
    String productName,
    String variationLabel,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal totalPrice,
    LocalDateTime soldAt
) {
    static SaleResponse from(Sale sale) {
        return new SaleResponse(
            sale.getId(),
            sale.getProductName(),
            sale.getVariationLabel(),
            sale.getQuantity(),
            sale.getUnitPrice(),
            sale.getTotalPrice(),
            sale.getSoldAt());
    }
}
