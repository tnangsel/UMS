package com.tenzin.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tenzin.dto.AuthenticationRequest;
import com.tenzin.dto.RoleAssignmentRequest;
import com.tenzin.dto.UserDao;
import com.tenzin.exceptions.UserNotFoundException;
import com.tenzin.services.AdminService;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
	
	@Autowired
	private AdminService adminService;
	
	@GetMapping("/user/{userId}")
	public ResponseEntity<UserDao> getUser(@PathVariable("userId") Integer userId) {
		try {
			UserDao user = adminService.findUserById(userId);
			return ResponseEntity.ok(user);
		}catch (UserNotFoundException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping("/users")
    public ResponseEntity<List<UserDao>> get() {
        List<UserDao> response = adminService.fetchAllUsers();
        return ResponseEntity.ok(response);
    }
	
	@PostMapping("/assign-role/{userId}")
	public ResponseEntity<String> setRoleToUser(@PathVariable("userId") Integer userId, @RequestBody RoleAssignmentRequest roleAssignmentRequest) {
        try {
            String response = adminService.assignRole(userId, roleAssignmentRequest.getRole(), roleAssignmentRequest.getPermissions());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while assigning the role.");
        }
    }
	
	@DeleteMapping("/delete-role/{userId}")
	public ResponseEntity<String> deleteUserRoles(@PathVariable("userId") Integer userId, @RequestBody RoleAssignmentRequest roleAssignmentRequest) {
        try {
            String response = adminService.deleteRoles(userId, roleAssignmentRequest.getRole());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while assigning the role.");
        }
	}
	
	@PutMapping("/deactivate")
	public ResponseEntity<String> deactivateUserAccount(@RequestBody AuthenticationRequest auth) {
		String response = adminService.lockUserAccount(auth);
		return ResponseEntity.ok(response);
	}
	
	@PutMapping("/activate")
	public ResponseEntity<String> activateUserAccount(@RequestBody AuthenticationRequest auth) {
		String response = adminService.unlockUserAccount(auth);
		return ResponseEntity.ok(response);
	}
	

}
