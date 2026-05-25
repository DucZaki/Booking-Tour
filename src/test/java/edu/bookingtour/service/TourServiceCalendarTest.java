package edu.bookingtour.service;

import edu.bookingtour.entity.Calendar;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TourServiceCalendarTest {

    @Test
    void pastFlagMatchesDateRelativeToToday() {
        TourService service = new TourService();
        List<Calendar> days = service.getCalendar(5, 2026, null, Collections.emptyList());
        LocalDate today = LocalDate.now();

        for (Calendar day : days) {
            if (!day.isCurrentMonth()) {
                continue;
            }
            if (day.getDate().isBefore(today)) {
                assertTrue(day.isPastDay(), day.getDate() + " should be past");
            } else {
                assertFalse(day.isPastDay(), day.getDate() + " should not be past");
            }
        }
    }
}
