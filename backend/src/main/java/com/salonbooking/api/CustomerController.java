package com.salonbooking.api;

import com.salonbooking.api.dto.CustomerResponse;
import com.salonbooking.api.dto.RegisterCustomerRequest;
import com.salonbooking.domain.Customer;
import com.salonbooking.repository.CustomerRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // POST /api/customers - SALON-01 Customer Registration
    @PostMapping
    public ResponseEntity<CustomerResponse> register(@Valid @RequestBody RegisterCustomerRequest request) {
        customerRepository.findByEmail(request.email()).ifPresent(existing -> {
            throw new DataIntegrityViolationException("Email already registered: " + request.email());
        });
        Customer customer = new Customer(request.name(), request.email(),
                passwordEncoder.encode(request.password()));
        Customer saved = customerRepository.save(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(CustomerResponse.from(saved));
    }
}
