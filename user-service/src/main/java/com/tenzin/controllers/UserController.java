package com.tenzin.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.tenzin.dto.UserDao;
import com.tenzin.services.UserService;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

	@Autowired
	private UserService userService;
	
	@GetMapping("/profile/{userId}")
	public ResponseEntity<UserDao> myProfile(@PathVariable Integer userId) {
		return ResponseEntity.ok(userService.getMyProfile(userId));
	}
	
	@PutMapping("/update")
	public ResponseEntity<String> updateProfile(@RequestBody UserDao user){
		ResponseEntity<String> response = userService.updateProfile(user);
		return response;
	}
	
	@PostMapping("/uploadProfilePicture/{userId}")
    public ResponseEntity<String> uploadProfilePicture(@PathVariable Integer userId,
                                                       @RequestParam("file") MultipartFile file) {
		ResponseEntity<String> response = userService.uploadProfilePicture(userId, file);
		return response;
	}
	
	
	
}
