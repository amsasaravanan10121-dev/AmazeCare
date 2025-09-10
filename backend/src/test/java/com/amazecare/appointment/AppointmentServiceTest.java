package com.amazecare.appointment;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;

class AppointmentServiceTest {
    private final AppointmentService service = new AppointmentService();

    @Test
    void accepts_time_within_hours() {
        assertDoesNotThrow(() -> service.validateWorkingHours(LocalTime.of(9, 0)));
        assertDoesNotThrow(() -> service.validateWorkingHours(LocalTime.of(17, 59)));
    }

    @Test
    void rejects_time_outside_hours() {
        assertThrows(IllegalArgumentException.class, () -> service.validateWorkingHours(LocalTime.of(8, 59)));
        assertThrows(IllegalArgumentException.class, () -> service.validateWorkingHours(LocalTime.of(18, 1)));
    }
}


