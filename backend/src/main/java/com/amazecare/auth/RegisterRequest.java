package com.amazecare.auth;

import com.amazecare.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
 

public record RegisterRequest(
        @NotBlank @Size(min = 4) @Pattern(regexp = "^[A-Za-z0-9_]+$") String username,
        @Email @NotBlank String email,
        @NotBlank @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$") String password,
        Role role
) {}


