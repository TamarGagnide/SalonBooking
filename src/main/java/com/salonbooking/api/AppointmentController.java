package com.salonbooking.api;

import com.salonbooking.api.dto.*;
import com.salonbooking.domain.Appointment;
import com.salonbooking.domain.Customer;
import com.salonbooking.domain.SalonService;
import com.salonbooking.domain.Stylist;
import com.salonbooking.repository.CustomerRepository;
import com.salonbooking.repository.SalonServiceRepository;
import com.salonbooking.repository.StylistRepository;
import com.salonbooking.service.AppointmentNotFoundException;
import com.salonbooking.service.AppointmentService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final CustomerRepository customerRepository;
    private final StylistRepository stylistRepository;
    private final SalonServiceRepository salonServiceRepository;
    private final com.salonbooking.repository.AppointmentRepository appointmentRepository;

    public AppointmentController(AppointmentService appointmentService,
                                  CustomerRepository customerRepository,
                                  StylistRepository stylistRepository,
                                  SalonServiceRepository salonServiceRepository,
                                  com.salonbooking.repository.AppointmentRepository appointmentRepository) {
        this.appointmentService = appointmentService;
        this.customerRepository = customerRepository;
        this.stylistRepository = stylistRepository;
        this.salonServiceRepository = salonServiceRepository;
        this.appointmentRepository = appointmentRepository;
    }

    // GET /api/stylists/{id}/availability - SALON-05 Check Appointment Availability
    @GetMapping("/api/stylists/{stylistId}/availability")
    public AvailabilityResponse checkAvailability(@PathVariable Long stylistId,
                                                    @RequestParam LocalDateTime startTime,
                                                    @RequestParam LocalDateTime endTime) {
        boolean available = appointmentService.isAvailable(stylistId, startTime, endTime);
        return new AvailabilityResponse(stylistId, startTime.toString(), endTime.toString(), available);
    }

    // POST /api/appointments - SALON-06 Book an Appointment
    @PostMapping("/api/appointments")
    public ResponseEntity<AppointmentResponse> bookAppointment(@Valid @RequestBody BookAppointmentRequest request) {
        Customer customer = findOrThrow(customerRepository, request.customerId());
        Stylist stylist = findOrThrow(stylistRepository, request.stylistId());
        SalonService service = findOrThrow(salonServiceRepository, request.serviceId());

        Appointment appointment = appointmentService.bookAppointment(
                customer, stylist, service, request.startTime(), request.endTime());

        return ResponseEntity.status(HttpStatus.CREATED).body(AppointmentResponse.from(appointment));
    }

    // DELETE /api/appointments/{id} - SALON-08 Cancel an Appointment
    @DeleteMapping("/api/appointments/{appointmentId}")
    public ResponseEntity<AppointmentResponse> cancelAppointment(@PathVariable Long appointmentId,
                                                                   @Valid @RequestBody CancelAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        appointmentService.cancelAppointment(appointment, request.requestingCustomerId());

        return ResponseEntity.ok(AppointmentResponse.from(appointment));
    }

    private static <T, ID> T findOrThrow(org.springframework.data.jpa.repository.JpaRepository<T, ID> repository, ID id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException(id + " not found"));
    }
}
