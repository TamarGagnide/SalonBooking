package com.salonbooking.api.dto;

import com.salonbooking.domain.SalonService;

import java.math.BigDecimal;

public record ServiceResponse(Long id, String name, String nameKa, int durationMinutes, BigDecimal price) {
    public static ServiceResponse from(SalonService service) {
        return new ServiceResponse(service.getId(), service.getName(), service.getNameKa(),
                service.getDurationMinutes(), service.getPrice());
    }
}
