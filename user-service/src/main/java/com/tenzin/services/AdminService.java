package com.tenzin.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tenzin.datatypes.Permission;
import com.tenzin.datatypes.RoleType;
import com.tenzin.dto.AuthenticationRequest;
import com.tenzin.dto.UserDao;
import com.tenzin.exceptions.UnauthorizedException;
import com.tenzin.exceptions.UserNotFoundException;
import com.tenzin.models.Role;
import com.tenzin.models.User;
import com.tenzin.repository.RoleRepository;
import com.tenzin.repository.UserRepository;

@Service
public class AdminService {

	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	/**
     * Assigns a role and its permissions to a user.
     *
     * @param userId       The ID of the user to whom the role is to be assigned.
     * @param string     The type of role to assign.
     * @param permissions  The set of permissions to assign.
     * @return A confirmation message.
     * @throws IllegalArgumentException if the user or role is not found.
     */
	
	@Transactional
	public String assignRole(Integer userId, RoleType roleName, Set<Permission> permissions) {
	    User user = userRepository.findById(userId)
	            .orElseThrow(() -> new IllegalArgumentException("User not found"));

	    // Fetch the existing role from the database
	    Role role = roleRepository.findByRole(roleName)
	            .orElseGet(() -> createRole(roleName, permissions));

	    // Update the user's roles and permissions
	    updateUserRolesAndPermissions(user, role, permissions);

	    return "Role " + roleName + " assigned to user " + user.getEmail() + " with permissions: " + permissions;
	}

	private Role createRole(RoleType roleName, Set<Permission> permissions) {
	    Role newRole = Role.builder()
	            .role(roleName)
	            .permissions(permissions)
	            .createdDate(LocalDateTime.now())
	            .build();
	    return roleRepository.save(newRole);
	}
	
	@Transactional
	private void updateUserRolesAndPermissions(User user, Role role, Set<Permission> permissions) {
	    // Check if the user already has this role
	    Optional<Role> existingUserRole = user.getRoles().stream()
	            .filter(r -> r.getRole().equals(role.getRole()))
	            .findFirst();

	    if (existingUserRole.isPresent()) {
	        // Update existing role with new permissions
	        Role existingRole = existingUserRole.get();
	        existingRole.setPermissions(permissions);
	        roleRepository.save(existingRole);
	    } else {
	        // Add the role to the user's roles
	        user.getRoles().add(role);
	        userRepository.save(user);
	    }
	}

	private String getAuthenticatedUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername(); 
        }
        throw new IllegalStateException("User not authenticated or principal is not UserDetails");
    }
	
	@Transactional
	public String deleteRoles(Integer userId, RoleType roleName) {
	    User user = userRepository.findById(userId)
	            .orElseThrow(() -> new IllegalArgumentException("User not found"));

	    Role roleToDelete = user.getRoles().stream()
	            .filter(r -> r.getRole() == roleName)
	            .findFirst()
	            .orElseThrow(() -> new IllegalArgumentException("Role not found for user " + user.getEmail()));

	    // Remove the specified permissions from the role
	    user.getRoles().remove(roleToDelete);
	    roleRepository.delete(roleToDelete);

	    return "Role "+ roleName + " were deleted from user " + user.getEmail();
	}

	public List<UserDao> fetchAllUsers() throws UnauthorizedException {

		List<User> users = userRepository.findAll();
		
		return users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
	}
	
	private UserDao convertToDto(User user) {
        return createUserDao(user);
	}

	private UserDao createUserDao(User user) {
		return UserDao.builder()
        		.id(String.valueOf(user.getId()))
        		.firstName(user.getFirstName())
        		.lastName(user.getLastName())
        		.email(user.getEmail())
        		.street(user.getAddress() != null ? user.getAddress().getStreetName(): null)
        		.city(user.getAddress() != null ? user.getAddress().getCityName(): null)
        		.state(String.valueOf(user.getAddress() != null ? user.getAddress().getState(): null))
        		.zipCode(String.valueOf(user.getAddress() != null ? user.getAddress().getZipcode(): null))
        		.country(user.getAddress() != null ? user.getAddress().getCountry(): null)
        		.phoneNumber(user.getPhoneNumber())
        		.accountLocked(user.isAccountLocked())
        		.enabled(user.isEnabled())
        		.createdDateTime(user.getCreatedDateTime())
        		.roles(user.getRoles())
        		.build();
	}
	
	public UserDao findUserById(Integer userId) {
		
		User existingUser = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User does not exist."));
		return createUserDao(existingUser);
	}

	@Transactional
	public String lockUserAccount(AuthenticationRequest auth) {
		User user = userRepository.findByEmail(auth.getEmail())
				.orElseThrow(() -> new UserNotFoundException("User not Found"));
		user.setAccountLocked(true);
		userRepository.save(user);
		return "User " + auth.getEmail() +"'s Account has been Deactivated!";
	}

	@Transactional
	public String unlockUserAccount(AuthenticationRequest auth) {
		User user = userRepository.findByEmail(auth.getEmail())
				.orElseThrow(() -> new UserNotFoundException("User not Found"));
		user.setAccountLocked(false);
		userRepository.save(user);
		return "User " + auth.getEmail() +"'s Account has been Unlocked!";
	}

	
	
	
	
	
	
	
}
