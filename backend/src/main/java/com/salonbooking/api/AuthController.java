package com.salonbooking.api;

import com.salonbooking.api.dto.CustomerResponse;
import com.salonbooking.domain.Customer;
import com.salonbooking.repository.CustomerRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CustomerRepository customerRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // POST /api/auth/login - SALON-02 Customer Login
    @PostMapping("/login")
    public ResponseEntity<CustomerResponse> login(@Valid @RequestBody LoginRequest request) {
        Customer customer = customerRepository.findByEmail(request.email())
                .filter(c -> passwordEncoder.matches(request.password(), c.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        return ResponseEntity.ok(CustomerResponse.from(customer));
    }
}
