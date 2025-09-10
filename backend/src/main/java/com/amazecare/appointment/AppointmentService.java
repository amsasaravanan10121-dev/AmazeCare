package com.amazecare.appointment;

import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class AppointmentService {
    public void validateWorkingHours(LocalTime time) {
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(18, 0);
        if (time.isBefore(start) || time.isAfter(end)) {
            throw new IllegalArgumentException("Appointment time must be between 9AM and 6PM");
        }
    }
}


