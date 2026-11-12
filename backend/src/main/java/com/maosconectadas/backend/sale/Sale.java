package com.maosconectadas.backend.sale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maosconectadas.backend.auth.UserAccount;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales")
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String productName;

    @Column(nullable = false, length = 250)
    private String variationLabel;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private LocalDateTime soldAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserAccount owner;

    protected Sale() {}

    public Sale(
        String productName,
        String variationLabel,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        LocalDateTime soldAt,
        UserAccount owner
    ) {
        this.productName = productName;
        this.variationLabel = variationLabel;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.soldAt = soldAt;
        this.owner = owner;
    }

    public Long getId() { return id; }
    public String getProductName() { return productName; }
    public String getVariationLabel() { return variationLabel; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public LocalDateTime getSoldAt() { return soldAt; }
    public UserAccount getOwner() { return owner; }
}
