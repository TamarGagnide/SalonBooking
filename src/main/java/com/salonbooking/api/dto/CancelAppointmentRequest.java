package com.salonbooking.api.dto;

import jakarta.validation.constraints.NotNull;

public record CancelAppointmentRequest(@NotNull Long requestingCustomerId) {
}
