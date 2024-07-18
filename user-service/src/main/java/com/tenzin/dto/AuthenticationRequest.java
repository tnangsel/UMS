package com.tenzin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {
	
	@NotNull(message = "Email is required")
	@Email(message = "Invalid email entered")
	private String email;
	
	@NotNull(message = "Password is required")
	private String password;
	
	private String smsNumber;
	private String smsMessage;
}
