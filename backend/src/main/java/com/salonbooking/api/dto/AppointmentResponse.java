package com.salonbooking.api.dto;

import com.salonbooking.domain.Appointment;

import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        Long customerId,
        Long stylistId,
        Long serviceId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status
) {
    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getCustomer().getId(),
                appointment.getStylist().getId(),
                appointment.getService().getId(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus().name());
    }
}
