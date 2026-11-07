package com.maosconectadas.backend.product;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    private Integer quantity;
    private String imageUrl;

    @ElementCollection
    @CollectionTable(name = "product_variations", joinColumns = @JoinColumn(name = "product_id"))
    private List<ProductVariation> variations = new ArrayList<>();

    public Product() {}

    public Product(String name, String description, String category, BigDecimal price, Integer quantity, String imageUrl) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
    public Integer getQuantity() { return quantity; }
    public String getImageUrl() { return imageUrl; }
    public List<ProductVariation> getVariations() { return variations; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setVariations(List<ProductVariation> variations) { this.variations = variations; }
}
