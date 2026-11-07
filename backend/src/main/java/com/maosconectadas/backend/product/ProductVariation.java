package com.maosconectadas.backend.product;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ProductVariation {
    private String size;
    private String color;

    @Column(nullable = false)
    private Integer quantity;

    public ProductVariation() {}

    public ProductVariation(String size, String color, Integer quantity) {
        this.size = size;
        this.color = color;
        this.quantity = quantity;
    }

    public String getSize() { return size; }
    public String getColor() { return color; }
    public Integer getQuantity() { return quantity; }
}
