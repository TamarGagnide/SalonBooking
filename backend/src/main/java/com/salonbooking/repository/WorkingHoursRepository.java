package com.salonbooking.repository;

import com.salonbooking.domain.WorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Long> {
    List<WorkingHours> findByStylistIdAndDayOfWeek(Long stylistId, DayOfWeek dayOfWeek);
}
