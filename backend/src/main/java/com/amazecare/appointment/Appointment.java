package com.amazecare.appointment;

import com.amazecare.doctor.Doctor;
import com.amazecare.patient.Patient;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Patient patient;

    @ManyToOne(optional = false)
    private Doctor doctor;

    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime time;

    private String status = "PENDING";


    private String symptoms;
    

    private String visitNature;


    private String consultationNotes;
    private String prescription;
    

    private String consultationFee;
    private boolean feePaid = false;

    public Appointment(Patient patient, Doctor doctor, LocalDate date, LocalTime time) {
        this.patient = patient;
        this.doctor = doctor;
        this.date = date;
        this.time = time;
        this.status = "PENDING";
    }
}


