package com.amazecare.doctor;

import com.amazecare.user.Role;
import com.amazecare.user.User;
import com.amazecare.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin
public class DoctorController {
    private final DoctorRepository doctorRepository;
    private final UserService userService;

    public DoctorController(DoctorRepository doctorRepository, UserService userService) {
        this.doctorRepository = doctorRepository;
        this.userService = userService;
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of("message", "Doctor controller is working", "timestamp", System.currentTimeMillis()));
    }

    @GetMapping
    public List<Doctor> list() { return doctorRepository.findAll(); }

    @GetMapping("/type/{type}")
    public List<Doctor> byType(@PathVariable DoctorType type) { return doctorRepository.findByType(type); }

    @PostMapping
    public ResponseEntity<Doctor> create(@Valid @RequestBody Doctor doctor) {
        return ResponseEntity.ok(doctorRepository.save(doctor));
    }

    @PostMapping("/register-doctor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerDoctor(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("=== DOCTOR REGISTRATION START ===");
            System.out.println("Request data: " + request);
            String name = (String) request.get("name");
            String specialty = (String) request.get("specialty");
            String experience = (String) request.get("experience");
            String qualification = (String) request.get("qualification");
            String designation = (String) request.get("designation");
            String email = (String) request.get("email");
            String phone = (String) request.get("phone");
            String address = (String) request.get("address");
            String consultationFee = (String) request.get("consultationFee");
            String username = (String) request.get("username");
            String password = (String) request.get("password");
            
            System.out.println("Extracted data - Name: " + name + ", Username: " + username + ", Specialty: " + specialty);
            

            if (name == null || name.trim().isEmpty()) {
                System.out.println("ERROR: Name is required");
                return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
            }
            if (username == null || username.trim().isEmpty()) {
                System.out.println("ERROR: Username is required");
                return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
            }
            if (password == null || password.trim().isEmpty()) {
                System.out.println("ERROR: Password is required");
                return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
            }
            

            try {
                userService.loadUserByUsername(username);
                System.out.println("ERROR: Username already exists: " + username);
                return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
            } catch (Exception e) {
                System.out.println("Username is available: " + username);
            }
            

            System.out.println("Creating user account...");
            User user = new User();
            user.setUsername(username.trim());
            user.setEmail(email != null ? email.trim() : username.trim() + "@doctor.local");
            user.setPassword(password);
            user.setRole(Role.DOCTOR);
            
            User savedUser = userService.register(user);
            System.out.println("User created successfully with ID: " + savedUser.getId());
            

            DoctorType doctorType = DoctorType.GENERAL_PHYSICIAN;
            if (specialty != null) {
                try {

                    switch (specialty.toLowerCase()) {
                        case "cardiology": doctorType = DoctorType.CARDIOLOGIST; break;
                        case "neurology": doctorType = DoctorType.NEUROLOGIST; break;
                        case "orthopedics": doctorType = DoctorType.ORTHOPEDIC; break;
                        case "pediatrics": doctorType = DoctorType.PEDIATRICIAN; break;
                        case "dermatology": doctorType = DoctorType.DERMATOLOGIST; break;
                        case "general medicine": doctorType = DoctorType.GENERAL_PHYSICIAN; break;
                        default: doctorType = DoctorType.GENERAL_PHYSICIAN; break;
                    }
                    System.out.println("Mapped specialty '" + specialty + "' to DoctorType: " + doctorType);
                } catch (Exception e) {
                    System.out.println("Invalid specialty, using default: " + specialty);
                }
            }
            

            System.out.println("Creating doctor record...");
            Doctor doctor = new Doctor();
            doctor.setName(name.trim());
            doctor.setType(doctorType);
            doctor.setPhone(phone != null ? phone.trim() : "0000000000");
            doctor.setSpecialty(specialty);
            doctor.setExperience(experience);
            doctor.setQualification(qualification);
            doctor.setDesignation(designation);
            doctor.setEmail(email);
            doctor.setAddress(address);
            doctor.setConsultationFee(consultationFee);
            doctor.setApproved(true); // Admin creates approved doctors
            doctor.setActive(true);
            

            Doctor savedDoctor = doctorRepository.save(doctor);
            savedDoctor.setUserId(savedUser.getId());
            savedDoctor = doctorRepository.save(savedDoctor);
            System.out.println("Doctor created successfully with ID: " + savedDoctor.getId());
            
            System.out.println("=== DOCTOR REGISTRATION SUCCESS ===");
            return ResponseEntity.ok(Map.of(
                "message", "Doctor registered successfully",
                "user", Map.of(
                    "id", savedUser.getId(),
                    "username", savedUser.getUsername(),
                    "email", savedUser.getEmail(),
                    "role", savedUser.getRole()
                ),
                "doctor", Map.of(
                    "id", savedDoctor.getId(),
                    "name", savedDoctor.getName(),
                    "specialty", savedDoctor.getSpecialty(),
                    "phone", savedDoctor.getPhone()
                )
            ));
            
        } catch (Exception e) {
            System.err.println("=== DOCTOR REGISTRATION ERROR ===");
            System.err.println("Error registering doctor: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to register doctor: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        return doctorRepository.findById(id)
                .map(d -> { d.setApproved(true); return ResponseEntity.ok(doctorRepository.save(d)); })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/resign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resign(@PathVariable Long id) {
        return doctorRepository.findById(id)
                .map(d -> { d.setActive(false); return ResponseEntity.ok(doctorRepository.save(d)); })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateDoctor(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            return doctorRepository.findById(id)
                .map(doctor -> {
                    if (request.containsKey("name")) doctor.setName((String) request.get("name"));
                    if (request.containsKey("specialty")) doctor.setSpecialty((String) request.get("specialty"));
                    if (request.containsKey("experience")) doctor.setExperience((String) request.get("experience"));
                    if (request.containsKey("qualification")) doctor.setQualification((String) request.get("qualification"));
                    if (request.containsKey("designation")) doctor.setDesignation((String) request.get("designation"));
                    if (request.containsKey("email")) doctor.setEmail((String) request.get("email"));
                    if (request.containsKey("phone")) doctor.setPhone((String) request.get("phone"));
                    if (request.containsKey("address")) doctor.setAddress((String) request.get("address"));
                    if (request.containsKey("consultationFee")) doctor.setConsultationFee((String) request.get("consultationFee"));
                    
                    Doctor updatedDoctor = doctorRepository.save(doctor);
                    return ResponseEntity.ok(Map.of("message", "Doctor updated successfully", "doctor", updatedDoctor));
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to update doctor: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        try {
            return doctorRepository.findById(id)
                .map(doctor -> {

                    Long linkedUserId = doctor.getUserId();
                    doctorRepository.deleteById(id);
                    if (linkedUserId != null) {
                        try {
                            userService.deleteById(linkedUserId);
                        } catch (Exception ignored) {}
                    }
                    return ResponseEntity.ok(Map.of("message", "Doctor deleted successfully"));
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to delete doctor: " + e.getMessage()));
        }
    }
}


