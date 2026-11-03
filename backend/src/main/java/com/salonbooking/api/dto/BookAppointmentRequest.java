package com.salonbooking.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record BookAppointmentRequest(
        @NotNull Long customerId,
        @NotNull Long stylistId,
        @NotNull Long serviceId,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime
) {
}
