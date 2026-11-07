package com.maosconectadas.backend.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VariationRequest(
    @NotBlank String size,
    @NotBlank String color,
    @NotNull @Min(0) Integer quantity
) {}
