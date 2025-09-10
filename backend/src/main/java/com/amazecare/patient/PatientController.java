package com.amazecare.patient;

import com.amazecare.appointment.AppointmentRepository;
import com.amazecare.user.Role;
import com.amazecare.user.User;
import com.amazecare.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin
public class PatientController {
    private final PatientRepository patientRepository;
    private final UserService userService;
    private final AppointmentRepository appointmentRepository;

    public PatientController(PatientRepository patientRepository, UserService userService, AppointmentRepository appointmentRepository) {
        this.patientRepository = patientRepository;
        this.userService = userService;
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping
    public List<Patient> list() { return patientRepository.findAll(); }

    @PostMapping
    public ResponseEntity<Patient> create(@Valid @RequestBody Patient patient) {
        if (patient.getPhone() != null && patient.getPhone().equals(patient.getEmergencyContact())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(patientRepository.save(patient));
    }

    @PostMapping("/register-patient-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerPatientByAdmin(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("Admin registering patient with data: " + request);
            

            String name = (String) request.get("name");
            String dateOfBirth = (String) request.get("dateOfBirth");
            String gender = (String) request.get("gender");
            String phone = (String) request.get("phone");
            String email = (String) request.get("email");
            String address = (String) request.get("address");
            String emergencyContact = (String) request.get("emergencyContact");
            String username = (String) request.get("username");
            String password = (String) request.get("password");
            

            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
            }
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
            }
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
            }
            

            try {
                userService.loadUserByUsername(username);
                System.out.println("ERROR: Username already exists: " + username);
                return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
            } catch (Exception e) {
                System.out.println("Username is available: " + username);
            }
            

            System.out.println("Creating User account for patient login...");
            User user = new User();
            user.setUsername(username.trim());
            user.setEmail(email != null ? email.trim() : username.trim() + "@patient.local");
            user.setPassword(password);
            user.setRole(Role.PATIENT);
            
            System.out.println("User details before registration:");
            System.out.println("   - Username: " + user.getUsername());
            System.out.println("   - Email: " + user.getEmail());
            System.out.println("   - Role: " + user.getRole());
            
            User savedUser;
            try {
                savedUser = userService.register(user);
                System.out.println("User account created successfully!");
                System.out.println("   - User ID: " + savedUser.getId());
                System.out.println("   - Username: " + savedUser.getUsername());
                System.out.println("   - Email: " + savedUser.getEmail());
                System.out.println("   - Role: " + savedUser.getRole());
            } catch (Exception e) {
                System.err.println("ERROR creating User account:");
                System.err.println("   - Error: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.badRequest().body(Map.of("error", "Failed to create user account: " + e.getMessage()));
            }
            

            int age = 25;
            if (dateOfBirth != null && !dateOfBirth.trim().isEmpty()) {
                try {
                    LocalDate birthDate = LocalDate.parse(dateOfBirth);
                    age = LocalDate.now().getYear() - birthDate.getYear();
                } catch (Exception e) {
                    System.out.println("Error parsing date of birth: " + e.getMessage());
                }
            }
            

            System.out.println("Creating Patient record with medical details...");
            Patient patient = new Patient();
            patient.setName(name.trim());
            patient.setGender(gender != null ? gender : "Not Specified");
            patient.setAge(age);
            patient.setAddress(address != null ? address.trim() : "Not Provided");
            patient.setPhone(phone != null ? phone.trim() : "0000000000");
            patient.setEmergencyContact(emergencyContact != null ? emergencyContact.trim() : "0000000000");
            
            Patient savedPatient = patientRepository.save(patient);
            savedPatient.setUserId(savedUser.getId());
            savedPatient = patientRepository.save(savedPatient);
            System.out.println("Patient record created successfully!");
            System.out.println("   - Patient ID: " + savedPatient.getId());
            System.out.println("   - Name: " + savedPatient.getName());
            System.out.println("   - Age: " + savedPatient.getAge());
            System.out.println("   - Phone: " + savedPatient.getPhone());
            
            System.out.println("Patient Registration Created Successfully!");
            System.out.println("   - User account created for login authentication");
            System.out.println("   - Patient record created for medical details");
            System.out.println("   - Patient can now login with username: " + savedUser.getUsername());
            
            return ResponseEntity.ok(Map.of(
                "message", "Patient registered successfully - both User and Patient records created",
                "user", Map.of(
                    "id", savedUser.getId(),
                    "username", savedUser.getUsername(),
                    "email", savedUser.getEmail(),
                    "role", savedUser.getRole()
                ),
                "patient", Map.of(
                    "id", savedPatient.getId(),
                    "name", savedPatient.getName(),
                    "phone", savedPatient.getPhone(),
                    "age", savedPatient.getAge()
                )
            ));
            
        } catch (Exception e) {
            System.err.println("Error registering patient: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to register patient: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePatient(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            return patientRepository.findById(id)
                .map(patient -> {
                    if (request.containsKey("name")) patient.setName((String) request.get("name"));
                    if (request.containsKey("gender")) patient.setGender((String) request.get("gender"));
                    if (request.containsKey("age")) patient.setAge((Integer) request.get("age"));
                    if (request.containsKey("address")) patient.setAddress((String) request.get("address"));
                    if (request.containsKey("phone")) patient.setPhone((String) request.get("phone"));
                    if (request.containsKey("emergencyContact")) patient.setEmergencyContact((String) request.get("emergencyContact"));
                    
                    Patient updatedPatient = patientRepository.save(patient);
                    return ResponseEntity.ok(Map.of("message", "Patient updated successfully", "patient", updatedPatient));
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to update patient: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePatient(@PathVariable Long id) {
        try {
            System.out.println("DELETING PATIENT");
            System.out.println("Patient ID: " + id);
            
            if (!patientRepository.existsById(id)) {
                System.out.println("Patient not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }
            

            Patient existing = patientRepository.findById(id).orElse(null);
            Long linkedUserId = existing != null ? existing.getUserId() : null;


            System.out.println("Deleting all appointments for patient ID: " + id);
            List<com.amazecare.appointment.Appointment> appointments = appointmentRepository.findByPatientId(id);
            System.out.println("Found " + appointments.size() + " appointments to delete");
            
            for (com.amazecare.appointment.Appointment appointment : appointments) {
                System.out.println("Deleting appointment ID: " + appointment.getId());
                appointmentRepository.deleteById(appointment.getId());
            }
            System.out.println("All appointments deleted successfully!");
            

            System.out.println("Deleting patient record...");
            patientRepository.deleteById(id);
            System.out.println("Patient record deleted successfully!");
            

            if (linkedUserId != null) {
                try {
                    userService.deleteById(linkedUserId);
                } catch (Exception ignored) {}
            }
            
            return ResponseEntity.ok(Map.of("message", "Patient and all associated appointments deleted successfully"));
        } catch (Exception e) {
            System.err.println("PATIENT DELETE ERROR");
            System.err.println("Error deleting patient: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to delete patient: " + e.getMessage()));
        }
    }


    @PutMapping("/me")
    public ResponseEntity<?> updateMyProfile(@RequestBody Map<String, Object> request) {
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !(auth.getPrincipal() instanceof com.amazecare.user.User user)) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }

            var optPatient = patientRepository.findByUserId(user.getId());
            if (optPatient.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Patient profile not found for current user"));
            }

            Patient patient = optPatient.get();

            if (request.containsKey("name")) patient.setName((String) request.get("name"));
            if (request.containsKey("gender")) patient.setGender((String) request.get("gender"));
            if (request.containsKey("age")) {
                Object v = request.get("age");
                if (v instanceof Integer) patient.setAge((Integer) v);
                else if (v instanceof Number) patient.setAge(((Number) v).intValue());
                else if (v instanceof String s) {
                    try { patient.setAge(Integer.parseInt(s)); } catch (Exception ignored) {}
                }
            }
            if (request.containsKey("address")) patient.setAddress((String) request.get("address"));
            if (request.containsKey("phone")) patient.setPhone((String) request.get("phone"));
            if (request.containsKey("emergencyContact")) patient.setEmergencyContact((String) request.get("emergencyContact"));
            if (request.containsKey("allergies")) patient.setAllergies((String) request.get("allergies"));
            if (request.containsKey("medicalHistory")) patient.setMedicalHistory((String) request.get("medicalHistory"));
            if (request.containsKey("currentMedications")) patient.setCurrentMedications((String) request.get("currentMedications"));

            if (request.containsKey("username")) {
                try {
                    var newUsername = (String) request.get("username");
                    user.setUsername(newUsername);
                    userService.updateUsername(user.getId(), newUsername);
                } catch (Exception ignored) {}
            }

            Patient updated = patientRepository.save(patient);
            return ResponseEntity.ok(Map.of("message", "Profile updated successfully", "patient", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to update profile: " + e.getMessage()));
        }
    }
}


