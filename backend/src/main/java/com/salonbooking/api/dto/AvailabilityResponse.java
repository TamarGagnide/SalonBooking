package com.salonbooking.api.dto;

public record AvailabilityResponse(Long stylistId, String startTime, String endTime, boolean available) {
}
