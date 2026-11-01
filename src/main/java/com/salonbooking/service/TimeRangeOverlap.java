package com.salonbooking.service;

import java.time.LocalDateTime;

/**
 * Pure time-range overlap check backing NFR-03 (no overlapping active
 * appointments). Ranges are half-open [start, end): a range ending exactly
 * when another starts is NOT an overlap, so back-to-back bookings are allowed.
 */
public final class TimeRangeOverlap {

    private TimeRangeOverlap() {
    }

    public static boolean isOverlapping(LocalDateTime firstStart, LocalDateTime firstEnd,
                                         LocalDateTime secondStart, LocalDateTime secondEnd) {
        return firstStart.isBefore(secondEnd) && secondStart.isBefore(firstEnd);
    }
}
