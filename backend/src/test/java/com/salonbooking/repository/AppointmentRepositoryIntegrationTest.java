package com.salonbooking.repository;

import com.salonbooking.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test against a real PostgreSQL instance (the docker-compose
 * "postgres" service), proving the DB-level EXCLUDE constraint in schema.sql
 * actually rejects a double-booking INSERT for NFR-03 - not just the
 * application-level check in AppointmentService, which is already covered by
 * unit tests. Requires `docker-compose up -d postgres` to be running first.
 * The host is overridable via -DintegrationTest.dbHost for environments
 * where "host.docker.internal" doesn't resolve (e.g. running natively on Linux).
 */
@SpringBootTest
class AppointmentRepositoryIntegrationTest {

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        String dbHost = System.getProperty("integrationTest.dbHost", "localhost");
        registry.add("spring.datasource.url", () -> "jdbc:postgresql://" + dbHost + ":5432/salonbooking");
        registry.add("spring.datasource.username", () -> "salonbooking");
        registry.add("spring.datasource.password", () -> "salonbooking");
    }

    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private StylistRepository stylistRepository;
    @Autowired
    private SalonServiceRepository salonServiceRepository;

    private Customer customerA;
    private Customer customerB;
    private Stylist stylist;
    private SalonService service;

    @BeforeEach
    void setUp() {
        customerA = customerRepository.save(new Customer("Ana", "ana-" + System.nanoTime() + "@example.com", "hash"));
        customerB = customerRepository.save(new Customer("Beka", "beka-" + System.nanoTime() + "@example.com", "hash"));
        stylist = stylistRepository.save(new Stylist("Nino-" + System.nanoTime()));
        service = salonServiceRepository.save(new SalonService("Haircut", 60, BigDecimal.valueOf(30)));
    }

    @Test
    void databaseRejectsSecondActiveAppointmentOverlappingTheFirstForSameStylist() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);
        appointmentRepository.save(new Appointment(customerA, stylist, service, start, end));

        LocalDateTime overlappingStart = start.plusMinutes(30);
        LocalDateTime overlappingEnd = end.plusMinutes(30);
        Appointment conflicting = new Appointment(customerB, stylist, service, overlappingStart, overlappingEnd);

        // Act & Assert: the database's EXCLUDE constraint fires, not application code
        assertThatThrownBy(() -> appointmentRepository.saveAndFlush(conflicting))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseAllowsBackToBackAppointmentsForSameStylist() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);
        appointmentRepository.save(new Appointment(customerA, stylist, service, start, end));

        Appointment backToBack = new Appointment(customerB, stylist, service, end, end.plusHours(1));

        // Act
        Appointment saved = appointmentRepository.saveAndFlush(backToBack);

        // Assert
        assertThat(saved.getId()).isNotNull();
    }
}
