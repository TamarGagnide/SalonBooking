package com.salonbooking.service;

import com.salonbooking.domain.*;
import com.salonbooking.repository.AppointmentRepository;
import com.salonbooking.repository.WorkingHoursRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final WorkingHoursRepository workingHoursRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                               WorkingHoursRepository workingHoursRepository) {
        this.appointmentRepository = appointmentRepository;
        this.workingHoursRepository = workingHoursRepository;
    }

    public boolean isAvailable(Long stylistId, LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            return false;
        }
        if (!isWithinWorkingHours(stylistId, start, end)) {
            return false;
        }
        List<Appointment> overlapping = appointmentRepository.findOverlapping(
                stylistId, start, end, AppointmentStatus.ACTIVE);
        return overlapping.isEmpty();
    }

    @Transactional
    public Appointment bookAppointment(Customer customer, Stylist stylist, SalonService service,
                                        LocalDateTime start, LocalDateTime end) {
        if (!isAvailable(stylist.getId(), start, end)) {
            throw new SlotUnavailableException(
                    "Stylist " + stylist.getId() + " is not available from " + start + " to " + end);
        }
        Appointment appointment = new Appointment(customer, stylist, service, start, end);
        try {
            return appointmentRepository.save(appointment);
        } catch (DataIntegrityViolationException raceConditionDetectedByDatabase) {
            throw new SlotUnavailableException(
                    "Stylist " + stylist.getId() + " was booked concurrently for this slot");
        }
    }

    @Transactional
    public void cancelAppointment(Appointment appointment, Long requestingCustomerId) {
        if (!appointment.getCustomer().getId().equals(requestingCustomerId)) {
            throw new UnauthorizedActionException(
                    "Customer " + requestingCustomerId + " cannot cancel another customer's appointment");
        }
        appointment.cancel();
        appointmentRepository.save(appointment);
    }

    private boolean isWithinWorkingHours(Long stylistId, LocalDateTime start, LocalDateTime end) {
        if (!start.toLocalDate().equals(end.toLocalDate())) {
            return false;
        }
        List<WorkingHours> dayHours = workingHoursRepository.findByStylistIdAndDayOfWeek(
                stylistId, start.getDayOfWeek());
        return dayHours.stream().anyMatch(wh ->
                !start.toLocalTime().isBefore(wh.getStartTime())
                        && !end.toLocalTime().isAfter(wh.getEndTime()));
    }
}
