package com.maosconectadas.backend.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maosconectadas.backend.auth.UserAccount;
import com.maosconectadas.backend.client.Client;
import com.maosconectadas.backend.product.Product;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private LocalDate expectedDate;

    @Column(length = 1000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDENTE;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserAccount owner;

    protected CustomerOrder() {}

    public CustomerOrder(
        Client client, Product product, Integer quantity, LocalDate expectedDate,
        String notes, UserAccount owner
    ) {
        this.client = client;
        this.product = product;
        this.quantity = quantity;
        this.expectedDate = expectedDate;
        this.notes = notes;
        this.owner = owner;
    }

    public Long getId() { return id; }
    public Client getClient() { return client; }
    public Product getProduct() { return product; }
    public Integer getQuantity() { return quantity; }
    public LocalDate getExpectedDate() { return expectedDate; }
    public String getNotes() { return notes; }
    public OrderStatus getStatus() { return status; }
    public UserAccount getOwner() { return owner; }
}
