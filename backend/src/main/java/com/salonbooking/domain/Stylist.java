package com.salonbooking.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "stylists")
public class Stylist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    protected Stylist() {
    }

    public Stylist(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
