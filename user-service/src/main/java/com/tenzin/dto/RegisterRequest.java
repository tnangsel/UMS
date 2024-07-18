package com.tenzin.dto;

import com.tenzin.datatypes.RoleType;
import com.tenzin.models.Address;

import jakarta.persistence.Embedded;
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
public class RegisterRequest {

	@NotNull(message = "Please enter your first name")
	private String firstname;
	
	@NotNull(message = "Please enter your last name")
	private String lastname;
	
	@NotNull(message = "Email is required")
	@Email(message = "Invalid email entered")
	private String email;
	
	@NotNull(message = "Password is required")
	private String password;
	
	@NotNull(message = "Select 2Factor Aunthentication")
	private boolean mfaEnabled;
	
	private String phoneNumber;
	private RoleType role;
	
	@Embedded
	private Address address;
}
