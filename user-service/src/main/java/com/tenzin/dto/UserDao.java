package com.tenzin.dto;

import java.time.LocalDateTime;
import java.util.Set;

import com.tenzin.models.Role;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDao {
	private String id;
    private String firstName;
    private String lastName;
    private String profilePicture;
    private String street; 
	private String city; 
	private String zipCode;
	private String state;
	private String country;
    private String email;
    private String password;
    private boolean mfaEnabled;
    private boolean accountLocked;
    private boolean enabled;
    private String phoneNumber;
    private LocalDateTime createdDateTime;
    private Set<Role> roles;
//    private Set<Permission> permissions;
}
