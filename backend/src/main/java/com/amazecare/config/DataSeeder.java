package com.amazecare.config;

import com.amazecare.appointment.Appointment;
import com.amazecare.appointment.AppointmentRepository;
import com.amazecare.doctor.Doctor;
import com.amazecare.doctor.DoctorRepository;
import com.amazecare.doctor.DoctorType;
import com.amazecare.patient.Patient;
import com.amazecare.patient.PatientRepository;
import com.amazecare.user.Role;
import com.amazecare.user.User;
import com.amazecare.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedData(UserRepository userRepository, PasswordEncoder encoder, DoctorRepository doctorRepository, PatientRepository patientRepository, AppointmentRepository appointmentRepository) {
        return args -> {
            try {
                if (!userRepository.existsByUsername("admin")) {
                    User admin = new User();
                    admin.setUsername("admin");
                    admin.setEmail("admin@amazecare.local");
                    admin.setPassword(encoder.encode("Admin@1234"));
                    admin.setRole(Role.ADMIN);
                    userRepository.save(admin);
                    System.out.println("Admin user created successfully: admin/Admin@1234");
                } else {
                    System.out.println("Admin user already exists");
                }


                if (doctorRepository.count() == 0) {
                    Doctor[] doctors = {
                            new Doctor("Dr. Sarah Johnson", DoctorType.CARDIOLOGIST, "555-0101", true, true),
                            new Doctor("Dr. Michael Chen", DoctorType.NEUROLOGIST, "555-0102", true, true),
                            new Doctor("Dr. Emily Rodriguez", DoctorType.PEDIATRICIAN, "555-0103", true, true),
                            new Doctor("Dr. David Wilson", DoctorType.ORTHOPEDIC, "555-0104", true, true),
                            new Doctor("Dr. Lisa Anderson", DoctorType.DERMATOLOGIST, "555-0105", true, true),
                            new Doctor("Dr. Robert Brown", DoctorType.GENERAL_PHYSICIAN, "555-0106", true, true)
                    };

                    for (Doctor doctor : doctors) {
                        doctorRepository.save(doctor);


                        String username = doctor.getName().replace("Dr. ", "").replace(" ", "").toLowerCase();
                        if (!userRepository.existsByUsername(username)) {
                            User doctorUser = new User();
                            doctorUser.setUsername(username);
                            doctorUser.setEmail(username + "@amazecare.local");
                            doctorUser.setPassword(encoder.encode("Doctor@1234"));
                            doctorUser.setRole(Role.DOCTOR);
                            userRepository.save(doctorUser);
                            System.out.println("Doctor user created: " + username + "/Doctor@1234");
                        }
                    }
                    System.out.println("Sample doctors and doctor users created successfully");
                } else {
                    System.out.println("Doctors already exist");
                }


                if (patientRepository.count() == 0) {
                    Patient patient1 = new Patient();
                    patient1.setName("John Doe");
                    patient1.setGender("Male");
                    patient1.setAge(35);
                    patient1.setAddress("123 Main St, City");
                    patient1.setPhone("1234567890");
                    patient1.setEmergencyContact("0987654321");

                    Patient patient2 = new Patient();
                    patient2.setName("Jane Smith");
                    patient2.setGender("Female");
                    patient2.setAge(28);
                    patient2.setAddress("456 Oak Ave, Town");
                    patient2.setPhone("2345678901");
                    patient2.setEmergencyContact("1876543210");

                    Patient patient3 = new Patient();
                    patient3.setName("Mike Johnson");
                    patient3.setGender("Male");
                    patient3.setAge(42);
                    patient3.setAddress("789 Pine Rd, Village");
                    patient3.setPhone("3456789012");
                    patient3.setEmergencyContact("2765432109");

                    patientRepository.save(patient1);
                    patientRepository.save(patient2);
                    patientRepository.save(patient3);
                    System.out.println("Sample patients created successfully");
                } else {
                    System.out.println("Patients already exist");
                }


                if (appointmentRepository.count() == 0) {
                    java.time.LocalDate today = java.time.LocalDate.now();
                    java.time.LocalTime time1 = java.time.LocalTime.of(9, 0);
                    java.time.LocalTime time2 = java.time.LocalTime.of(10, 30);
                    java.time.LocalTime time3 = java.time.LocalTime.of(14, 0);


                    Doctor firstDoctor = doctorRepository.findAll().get(0);
                    Patient firstPatient = patientRepository.findAll().get(0);
                    Patient secondPatient = patientRepository.findAll().get(1);

                    Appointment appointment1 = new Appointment();
                    appointment1.setPatient(firstPatient);
                    appointment1.setDoctor(firstDoctor);
                    appointment1.setDate(today);
                    appointment1.setTime(time1);

                    Appointment appointment2 = new Appointment();
                    appointment2.setPatient(secondPatient);
                    appointment2.setDoctor(firstDoctor);
                    appointment2.setDate(today);
                    appointment2.setTime(time2);

                    Appointment appointment3 = new Appointment();
                    appointment3.setPatient(firstPatient);
                    appointment3.setDoctor(firstDoctor);
                    appointment3.setDate(today);
                    appointment3.setTime(time3);

                    appointmentRepository.save(appointment1);
                    appointmentRepository.save(appointment2);
                    appointmentRepository.save(appointment3);
                    System.out.println("Sample appointments created successfully");
                } else {
                    System.out.println("Appointments already exist");
                }
            } catch (Exception e) {
                System.err.println("Failed to seed data: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}


