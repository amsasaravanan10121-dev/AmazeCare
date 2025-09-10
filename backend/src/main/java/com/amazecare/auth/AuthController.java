package com.amazecare.auth;

import com.amazecare.security.JwtService;
import com.amazecare.user.Role;
import com.amazecare.user.User;
import com.amazecare.user.UserService;
import com.amazecare.patient.Patient;
import com.amazecare.patient.PatientRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final PatientRepository patientRepository;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtService jwtService, PatientRepository patientRepository) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
        this.patientRepository = patientRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        User user = new User();
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPassword(req.password());
        user.setRole(req.role() == null ? Role.PATIENT : req.role());
        User saved = userService.register(user);
        String token = jwtService.generateToken(saved.getUsername(), Map.of("role", saved.getRole().name()));
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/register-patient")
    public ResponseEntity<?> registerPatient(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("Registering patient with data: " + request);
            
            String username = (String) request.get("username");
            String password = (String) request.get("password");
            String name = (String) request.get("name");
            String email = (String) request.get("email");
            String phone = (String) request.get("phone");
            String address = (String) request.get("address");
            String emergencyContact = (String) request.get("emergencyContact");
            String gender = (String) request.get("gender");
            String dateOfBirth = (String) request.get("dateOfBirth");

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
            }
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
            }
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
            }
            
            try {
                userService.loadUserByUsername(username.trim());
                return ResponseEntity.badRequest().body(Map.of("error", "Username already exists. Please choose a different username."));
            } catch (Exception ignored) {}

            User user = new User();
            user.setUsername(username.trim());
            user.setEmail(email != null ? email.trim() : username.trim() + "@patient.local");
            user.setPassword(password);
            user.setRole(Role.PATIENT);

            User savedUser = userService.register(user);
            System.out.println("User created with ID: " + savedUser.getId());

            int age = 25;
            if (dateOfBirth != null && !dateOfBirth.trim().isEmpty()) {
                try {
                    LocalDate birthDate = LocalDate.parse(dateOfBirth);
                    age = LocalDate.now().getYear() - birthDate.getYear();
                } catch (Exception e) {
                    System.out.println("Error parsing date of birth: " + e.getMessage());
                }
            }
            

            Patient patient = new Patient();
            patient.setName(name.trim());
            patient.setGender(gender != null ? gender : "Not Specified");
            patient.setAge(age);
            patient.setAddress(address != null ? address.trim() : "Not Provided");
            patient.setPhone(phone != null ? phone.trim() : "0000000000");
            patient.setEmergencyContact(emergencyContact != null ? emergencyContact.trim() : "0000000000");
            

            patient.setUserId(savedUser.getId());
            Patient savedPatient = patientRepository.save(patient);
            System.out.println("Patient created with ID: " + savedPatient.getId());
            

            String token = jwtService.generateToken(savedUser.getUsername(), Map.of("role", savedUser.getRole().name()));
            
            return ResponseEntity.ok(Map.of(
                "message", "Patient registered successfully",
                "token", token,
                "user", Map.of(
                    "id", savedUser.getId(),
                    "username", savedUser.getUsername(),
                    "email", savedUser.getEmail(),
                    "role", savedUser.getRole()
                ),
                "patient", Map.of(
                    "id", savedPatient.getId(),
                    "name", savedPatient.getName(),
                    "phone", savedPatient.getPhone()
                )
            ));
            
        } catch (Exception e) {
            System.err.println("Error registering patient: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to register patient: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            System.out.println("Login attempt for username: " + req.username());
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password()));
            

            User user = (User) authentication.getPrincipal();
            System.out.println("Authentication successful for user: " + user.getUsername() + " with role: " + user.getRole());
            
            String token = jwtService.generateToken(req.username(), Map.of("role", user.getRole().name()));
            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) {
            System.err.println("Authentication failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
    }

    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdmin() {
        try {

            if (userService.loadUserByUsername("admin") != null) {
                return ResponseEntity.ok(Map.of("message", "Admin user already exists", "username", "admin"));
            }
            
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@amazecare.local");
            admin.setPassword("Admin@1234");
            admin.setRole(Role.ADMIN);
            User saved = userService.register(admin);
            return ResponseEntity.ok(Map.of("message", "Admin user created successfully", "username", saved.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to create admin user: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "role", user.getRole()
                ));
            } else {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error getting current user: " + e.getMessage()));
        }
    }

    @GetMapping("/check-admin")
    public ResponseEntity<?> checkAdmin() {
        try {
            User admin = (User) userService.loadUserByUsername("admin");
            if (admin != null) {
                return ResponseEntity.ok(Map.of(
                    "message", "Admin user exists",
                    "username", admin.getUsername(),
                    "email", admin.getEmail(),
                    "role", admin.getRole(),
                    "passwordHash", admin.getPassword().substring(0, 20) + "..."
                ));
            } else {
                return ResponseEntity.ok(Map.of("message", "Admin user does not exist"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error checking admin user: " + e.getMessage()));
        }
    }
}


