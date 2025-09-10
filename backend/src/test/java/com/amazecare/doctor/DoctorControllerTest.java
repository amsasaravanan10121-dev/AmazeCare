package com.amazecare.doctor;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DoctorController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DoctorRepository doctorRepository;

    @Test
    void list_returns_doctors() throws Exception {
        Doctor d = new Doctor();
        d.setId(1L);
        d.setName("Alice");
        d.setPhone("1234567890");
        d.setType(DoctorType.CARDIOLOGIST);
        Mockito.when(doctorRepository.findAll()).thenReturn(List.of(d));

        mockMvc.perform(get("/api/doctors").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[0].type").value("CARDIOLOGIST"));
    }
}


