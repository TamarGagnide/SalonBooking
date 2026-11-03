package com.salonbooking.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "stylist_id", nullable = false)
    private Stylist stylist;

    @ManyToOne(optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private SalonService service;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.ACTIVE;

    protected Appointment() {
    }

    public Appointment(Customer customer, Stylist stylist, SalonService service,
                        LocalDateTime startTime, LocalDateTime endTime) {
        this.customer = customer;
        this.stylist = stylist;
        this.service = service;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void cancel() {
        this.status = AppointmentStatus.CANCELLED;
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Stylist getStylist() {
        return stylist;
    }

    public SalonService getService() {
        return service;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }
}
