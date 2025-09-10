package com.amazecare.appointment;

import com.amazecare.doctor.DoctorRepository;
import com.amazecare.patient.PatientRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin
public class AppointmentController {
    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentController(AppointmentRepository appointmentRepository, AppointmentService appointmentService, PatientRepository patientRepository, DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentService = appointmentService;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @GetMapping
    public List<Appointment> list() { 
        return appointmentRepository.findAllWithPatientAndDoctor();
    }
    
    @GetMapping("/doctor/{doctorId}")
    public List<Appointment> getAppointmentsByDoctor(@PathVariable Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }
    
    @GetMapping("/doctor/{doctorId}/today")
    public List<Appointment> getTodayAppointmentsByDoctor(@PathVariable Long doctorId) {
        return appointmentRepository.findByDoctorIdAndDate(doctorId, java.time.LocalDate.now());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody AppointmentRequest request) {
        try {
            System.out.println("Creating appointment for patient: " + request.getPatientName());
            appointmentService.validateWorkingHours(request.getTime());
            

            com.amazecare.patient.Patient patient = null;
            if (request.getPatientId() != null && request.getPatientId() > 0) {
                patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
                System.out.println("Found existing patient with ID: " + patient.getId());
            } else {
                Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (principal instanceof com.amazecare.user.User user) {
                    patient = patientRepository.findByUserId(user.getId()).orElse(null);
                }
                if (patient == null) {
                    var candidates = patientRepository.findByNameIgnoreCaseAndUserIdIsNull(request.getPatientName());
                    if (!candidates.isEmpty() && principal instanceof com.amazecare.user.User user2) {
                        patient = candidates.get(0);
                        patient.setUserId(user2.getId());
                        patient = patientRepository.save(patient);
                        System.out.println("Linked existing patient record '" + request.getPatientName() + "' to user id " + user2.getId());
                    } else {
                        throw new RuntimeException("Patient linkage missing. Please complete patient profile before booking.");
                    }
                }
            }
            
            com.amazecare.doctor.Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
            System.out.println("Found doctor: " + doctor.getName() + " with ID: " + doctor.getId());
            
            Appointment appointment = new Appointment();
            appointment.setPatient(patient);
            appointment.setDoctor(doctor);
            appointment.setDate(request.getDate());
            appointment.setTime(request.getTime());
            appointment.setSymptoms(request.getSymptoms());
            appointment.setVisitNature(request.getVisitNature());
            
            System.out.println("Saving appointment for patient ID: " + patient.getId() + ", doctor ID: " + doctor.getId());
            Appointment savedAppointment = appointmentRepository.save(appointment);
            System.out.println("Appointment saved with ID: " + savedAppointment.getId());
            
            return ResponseEntity.ok(savedAppointment);
        } catch (Exception e) {
            System.err.println("Error creating appointment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating appointment: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAppointment(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            System.out.println("=== UPDATING APPOINTMENT ===");
            System.out.println("Appointment ID: " + id);
            System.out.println("Request data: " + request);
            
            return appointmentRepository.findById(id)
                .map(appointment -> {
                    System.out.println("Found appointment: " + appointment.getId());
                    
                    if (request.containsKey("date")) {
                        String dateStr = (String) request.get("date");
                        System.out.println("Updating date to: " + dateStr);
                        appointment.setDate(java.time.LocalDate.parse(dateStr));
                    }
                    if (request.containsKey("time")) {
                        String timeStr = (String) request.get("time");
                        System.out.println("Updating time to: " + timeStr);
                        appointment.setTime(java.time.LocalTime.parse(timeStr));
                    }
                    if (request.containsKey("status")) {
                        String statusStr = (String) request.get("status");
                        System.out.println("Updating status to: " + statusStr);
                        appointment.setStatus(statusStr);
                    }
                    if (request.containsKey("consultationNotes")) {
                        String notes = (String) request.get("consultationNotes");
                        System.out.println("Setting consultation notes");
                        appointment.setConsultationNotes(notes);
                    }
                    if (request.containsKey("prescription")) {
                        String pres = (String) request.get("prescription");
                        System.out.println("Setting prescription");
                        appointment.setPrescription(pres);
                    }
                    
                    if (request.containsKey("status") && "COMPLETED".equals(request.get("status"))) {
                        String fee = appointment.getDoctor().getConsultationFee();
                        if (fee != null && !fee.isEmpty()) {
                            appointment.setConsultationFee(fee);
                            System.out.println("Setting consultation fee: " + fee);
                        }
                    }
                    
                    System.out.println("Saving appointment...");
                    Appointment updatedAppointment = appointmentRepository.save(appointment);
                    System.out.println("Appointment updated successfully with ID: " + updatedAppointment.getId());
                    
                    return ResponseEntity.ok(Map.of("message", "Appointment updated successfully", "appointment", updatedAppointment));
                })
                .orElseGet(() -> {
                    System.out.println("Appointment not found with ID: " + id);
                    return ResponseEntity.notFound().build();
                });
        } catch (Exception e) {
            System.err.println("APPOINTMENT UPDATE ERROR ");
            System.err.println("Error updating appointment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to update appointment: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<?> payConsultationFee(@PathVariable Long id) {
        try {
            System.out.println("Processing payment for appointment ID: " + id);
            
            return appointmentRepository.findById(id)
                .map(appointment -> {
                    if (!"COMPLETED".equals(appointment.getStatus())) {
                        return ResponseEntity.badRequest().body(Map.of("error", "Cannot pay for incomplete consultation"));
                    }
                    
                    appointment.setFeePaid(true);
                    Appointment updatedAppointment = appointmentRepository.save(appointment);
                    System.out.println("Payment processed successfully for appointment ID: " + id);
                    
                    return ResponseEntity.ok(Map.of("message", "Payment processed successfully", "appointment", updatedAppointment));
                })
                .orElseGet(() -> {
                    System.out.println("Appointment not found with ID: " + id);
                    return ResponseEntity.notFound().build();
                });
        } catch (Exception e) {
            System.err.println("Error processing payment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to process payment: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id) {
        try {
            if (appointmentRepository.existsById(id)) {
                appointmentRepository.deleteById(id);
                return ResponseEntity.ok(Map.of("message", "Appointment deleted successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to delete appointment: " + e.getMessage()));
        }
    }
}


