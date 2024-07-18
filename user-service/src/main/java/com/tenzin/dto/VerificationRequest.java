package com.tenzin.dto;

import org.springframework.format.annotation.NumberFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerificationRequest {

	@NotNull(message = "Email is required")
	@Email(message = "Invalid email")
    private String email;
	@NotNull(message = "OTP code is required")
	@NumberFormat
    private String code;
	
}
