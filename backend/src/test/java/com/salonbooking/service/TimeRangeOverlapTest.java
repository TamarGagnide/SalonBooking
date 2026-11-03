package com.salonbooking.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TimeRangeOverlapTest {

    private static final LocalDateTime DAY = LocalDateTime.of(2026, 11, 2, 0, 0);

    @Test
    void returnsTrueWhenRangesOverlap() {
        // Arrange
        LocalDateTime firstStart = DAY.withHour(10);
        LocalDateTime firstEnd = DAY.withHour(11);
        LocalDateTime secondStart = DAY.withHour(10).withMinute(30);
        LocalDateTime secondEnd = DAY.withHour(11).withMinute(30);

        // Act
        boolean result = TimeRangeOverlap.isOverlapping(firstStart, firstEnd, secondStart, secondEnd);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void returnsFalseWhenSecondRangeStartsExactlyWhenFirstEnds() {
        // Arrange
        LocalDateTime firstStart = DAY.withHour(10);
        LocalDateTime firstEnd = DAY.withHour(11);
        LocalDateTime secondStart = DAY.withHour(11);
        LocalDateTime secondEnd = DAY.withHour(12);

        // Act
        boolean result = TimeRangeOverlap.isOverlapping(firstStart, firstEnd, secondStart, secondEnd);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void returnsFalseWhenRangesAreFarApart() {
        // Arrange
        LocalDateTime firstStart = DAY.withHour(9);
        LocalDateTime firstEnd = DAY.withHour(10);
        LocalDateTime secondStart = DAY.withHour(14);
        LocalDateTime secondEnd = DAY.withHour(15);

        // Act
        boolean result = TimeRangeOverlap.isOverlapping(firstStart, firstEnd, secondStart, secondEnd);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void returnsTrueWhenOneRangeFullyContainsTheOther() {
        // Arrange
        LocalDateTime firstStart = DAY.withHour(9);
        LocalDateTime firstEnd = DAY.withHour(17);
        LocalDateTime secondStart = DAY.withHour(10);
        LocalDateTime secondEnd = DAY.withHour(11);

        // Act
        boolean result = TimeRangeOverlap.isOverlapping(firstStart, firstEnd, secondStart, secondEnd);

        // Assert
        assertThat(result).isTrue();
    }
}
