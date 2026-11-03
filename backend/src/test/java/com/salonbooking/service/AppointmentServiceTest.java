package com.salonbooking.service;

import com.salonbooking.domain.*;
import com.salonbooking.repository.AppointmentRepository;
import com.salonbooking.repository.WorkingHoursRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private WorkingHoursRepository workingHoursRepository;

    private AppointmentService appointmentService;

    private Customer customer;
    private Stylist stylist;
    private SalonService service;

    // A Monday, so DayOfWeek.MONDAY working hours line up with it.
    private static final LocalDateTime MONDAY_10AM = LocalDateTime.of(2026, 11, 2, 10, 0);
    private static final LocalDateTime MONDAY_11AM = LocalDateTime.of(2026, 11, 2, 11, 0);

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(appointmentRepository, workingHoursRepository);
        customer = new Customer("Ana", "ana@example.com", "hash");
        stylist = new Stylist("Nino");
        service = new SalonService("Haircut", 60, BigDecimal.valueOf(30));
    }

    private void givenStylistWorksAllDay() {
        WorkingHours workingHours = new WorkingHours(stylist, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(18, 0));
        when(workingHoursRepository.findByStylistIdAndDayOfWeek(any(), any()))
                .thenReturn(List.of(workingHours));
    }

    @Test
    void bookAppointment_whenSlotIsFree_savesAndReturnsAppointment() {
        // Arrange
        givenStylistWorksAllDay();
        when(appointmentRepository.findOverlapping(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Appointment result = appointmentService.bookAppointment(
                customer, stylist, service, MONDAY_10AM, MONDAY_11AM);

        // Assert
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.ACTIVE);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void bookAppointment_whenSlotOverlapsExistingAppointment_throwsSlotUnavailable() {
        // Arrange
        givenStylistWorksAllDay();
        Appointment existing = new Appointment(customer, stylist, service, MONDAY_10AM, MONDAY_11AM);
        when(appointmentRepository.findOverlapping(any(), any(), any(), any()))
                .thenReturn(List.of(existing));

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.bookAppointment(
                customer, stylist, service, MONDAY_10AM, MONDAY_11AM))
                .isInstanceOf(SlotUnavailableException.class);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void bookAppointment_whenOutsideStylistWorkingHours_throwsSlotUnavailable() {
        // Arrange: no working-hours row at all for Monday => stylist is closed that day
        when(workingHoursRepository.findByStylistIdAndDayOfWeek(any(), any()))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.bookAppointment(
                customer, stylist, service, MONDAY_10AM, MONDAY_11AM))
                .isInstanceOf(SlotUnavailableException.class);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void bookAppointment_whenDatabaseRejectsConcurrentDoubleBooking_throwsSlotUnavailable() {
        // Arrange: app-level check passes, but the DB exclusion constraint catches
        // a race condition from a simultaneous request (NFR-03 under concurrency).
        givenStylistWorksAllDay();
        when(appointmentRepository.findOverlapping(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any(Appointment.class)))
                .thenThrow(new DataIntegrityViolationException("exclusion constraint violated"));

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.bookAppointment(
                customer, stylist, service, MONDAY_10AM, MONDAY_11AM))
                .isInstanceOf(SlotUnavailableException.class);
    }

    @Test
    void cancelAppointment_whenRequestedByOwningCustomer_marksAppointmentCancelled() {
        // Arrange
        Appointment appointment = new Appointment(customer, stylist, service, MONDAY_10AM, MONDAY_11AM);
        setId(customer, 1L);

        // Act
        appointmentService.cancelAppointment(appointment, 1L);

        // Assert
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void cancelAppointment_whenRequestedByDifferentCustomer_throwsUnauthorized() {
        // Arrange
        Appointment appointment = new Appointment(customer, stylist, service, MONDAY_10AM, MONDAY_11AM);
        setId(customer, 1L);

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.cancelAppointment(appointment, 999L))
                .isInstanceOf(UnauthorizedActionException.class);
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.ACTIVE);
        verify(appointmentRepository, never()).save(any());
    }

    /** Test-only helper: Customer.id is DB-generated and has no public setter. */
    private static void setId(Customer customer, Long id) {
        try {
            var field = Customer.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(customer, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
