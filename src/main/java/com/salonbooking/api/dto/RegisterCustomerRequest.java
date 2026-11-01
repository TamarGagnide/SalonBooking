package com.salonbooking.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterCustomerRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotBlank String password
) {
}
