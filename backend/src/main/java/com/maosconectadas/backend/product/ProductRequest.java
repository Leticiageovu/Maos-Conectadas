package com.maosconectadas.backend.product;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 120)
    String name,

    @Size(max = 1000)
    String description,

    @NotBlank(message = "Categoria é obrigatória")
    String category,

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Preço deve ser maior que zero")
    BigDecimal price,

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 0, message = "Quantidade não pode ser negativa")
    Integer quantity,

    String imageUrl,

    List<VariationRequest> variations
) {}
