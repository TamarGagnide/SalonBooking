package com.salonbooking.repository;

import com.salonbooking.domain.Appointment;
import com.salonbooking.domain.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("""
        select a from Appointment a
        where a.stylist.id = :stylistId
          and a.status = :status
          and a.startTime < :endTime
          and a.endTime > :startTime
        """)
    List<Appointment> findOverlapping(@Param("stylistId") Long stylistId,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime,
                                       @Param("status") AppointmentStatus status);
}
