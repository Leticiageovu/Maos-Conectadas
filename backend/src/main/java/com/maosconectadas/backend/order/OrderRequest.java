package com.maosconectadas.backend.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record OrderRequest(
    @NotNull(message = "Cliente é obrigatória")
    Long clientId,

    @NotNull(message = "Produto é obrigatório")
    Long productId,

    @NotNull(message = "Quantidade é obrigatória")
    @Positive(message = "Quantidade deve ser maior que zero")
    Integer quantity,

    @NotNull(message = "Previsão de entrega é obrigatória")
    LocalDate expectedDate,

    @Size(max = 1000)
    String notes
) {}
