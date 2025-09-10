package com.amazecare.patient;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3)
    @Pattern(regexp = "^[A-Za-z ]+$")
    private String name;

    @NotBlank
    private String gender;

    @NotBlank
    private String address;

    @NotBlank
    private String phone;

    @NotBlank
    private String emergencyContact;

    private int age;

    private String allergies;
    private String medicalHistory;
    private String currentMedications;


    @Column(name = "user_id")
    private Long userId;

    public Patient(String name, String gender, int age, String address, String phone, String emergencyContact) {
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.address = address;
        this.phone = phone;
        this.emergencyContact = emergencyContact;
    }
}


