package com.maosconectadas.backend.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maosconectadas.backend.auth.UserAccount;
import jakarta.persistence.*;

@Entity
@Table(name = "clients")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(length = 254)
    private String email;

    @Column(length = 1000)
    private String notes;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserAccount owner;

    protected Client() {}

    public Client(String name, String phone, String email, String notes, UserAccount owner) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.notes = notes;
        this.owner = owner;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getNotes() { return notes; }
    public UserAccount getOwner() { return owner; }

    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setNotes(String notes) { this.notes = notes; }
}
