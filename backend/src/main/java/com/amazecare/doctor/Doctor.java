package com.amazecare.doctor;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3)
    @Pattern(regexp = "^[A-Za-z .]+$")
    private String name;

    @Enumerated(EnumType.STRING)
    @NotNull
    private DoctorType type;

    @NotBlank
    private String phone;

    private boolean approved = false;
    private boolean active = true;


    private String specialty;
    private String experience;
    private String qualification;
    private String designation;
    private String email;
    private String address;
    private String consultationFee;


    @Column(name = "user_id")
    private Long userId;

    public Doctor(String name, DoctorType type, String phone, boolean approved, boolean active) {
        this.name = name;
        this.type = type;
        this.phone = phone;
        this.approved = approved;
        this.active = active;
    }

    public Doctor(String name, DoctorType type, String phone, boolean approved, boolean active, 
                  String specialty, String experience, String qualification, String designation, 
                  String email, String address, String consultationFee) {
        this.name = name;
        this.type = type;
        this.phone = phone;
        this.approved = approved;
        this.active = active;
        this.specialty = specialty;
        this.experience = experience;
        this.qualification = qualification;
        this.designation = designation;
        this.email = email;
        this.address = address;
        this.consultationFee = consultationFee;
    }
}


