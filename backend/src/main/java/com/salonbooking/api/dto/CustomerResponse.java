package com.salonbooking.api.dto;

import com.salonbooking.domain.Customer;

public record CustomerResponse(Long id, String name, String email) {
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getName(), customer.getEmail());
    }
}
