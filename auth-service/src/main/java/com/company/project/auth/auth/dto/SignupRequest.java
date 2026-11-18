package com.company.project.auth.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Email @Size(max = 255) String email,
        // BCrypt only uses the first 72 bytes of a password.
        @NotBlank @Size(min = 8, max = 72) String password) {

    /** Never log passwords - records print every component by default. */
    @Override
    public String toString() {
        return "SignupRequest[fullName=" + fullName + ", email=" + email + ", password=****]";
    }
}
