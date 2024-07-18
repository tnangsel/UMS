package com.tenzin.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.tenzin.dto.UserDao;
import com.tenzin.exceptions.UnauthorizedException;
import com.tenzin.exceptions.UserNotFoundException;
import com.tenzin.models.User;
import com.tenzin.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService {
    
    @Value("${spring.servlet.multipart.max-file-size}")
    private long maxFileSize;

    @Value("${upload.directory}")
    private String uploadProfileDirectory;

    @Value("${upload.allowed-extensions}")
    private String[] allowedExtensions;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public UserDao getMyProfile(Integer userId) throws UserNotFoundException, UnauthorizedException {
		
    	User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
    	// Get the ID of the currently authenticated user
        String authenticatedUserEmail = getAuthenticatedUserEmail();

        // Check if the authenticated user is requesting their own profile
        if (!authenticatedUserEmail.equals(user.getEmail())) {
            throw new UnauthorizedException("You are not authorized to view this profile");
        }
    	
		UserDao uDao = UserDao.builder()
								.firstName(user.getFirstName())
								.lastName(user.getLastName())
								.email(user.getEmail())
								.id(String.valueOf(user.getId()))
								.mfaEnabled(user.isMfaEnabled())
								.accountLocked(user.isAccountLocked())
								.street(user.getAddress() != null ? user.getAddress().getStreetName(): null)
				        		.city(user.getAddress() != null ? user.getAddress().getCityName(): null)
				        		.state(String.valueOf(user.getAddress() != null ? user.getAddress().getState(): null))
				        		.zipCode(String.valueOf(user.getAddress() != null ? user.getAddress().getZipcode(): null))
				        		.country(user.getAddress() != null ? user.getAddress().getCountry(): null)
								.createdDateTime(user.getCreatedDateTime())
								.roles(user.getRoles())
								.build();
		return  uDao;
	}

    private String getAuthenticatedUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername(); 
        }
        throw new IllegalStateException("User not authenticated or principal is not UserDetails");
    }
    
    public ResponseEntity<String> updateProfile(UserDao user) {
    	
    	User eUser = userRepository.findByEmail(user.getEmail()).orElseThrow(() -> new UserNotFoundException("User not found"));
    	// Get the ID of the currently authenticated user
        String authenticatedUserEmail = getAuthenticatedUserEmail();

        // Check if the authenticated user is requesting their own profile
        if (!authenticatedUserEmail.equals(eUser.getEmail())) {
            throw new UnauthorizedException("You are not authorized to view this profile");
        }
        
    	try {
	    	eUser.setAccountLocked(user.isAccountLocked());
	    	eUser.setFirstName(user.getFirstName());
	    	eUser.setLastName(user.getLastName());
	    	eUser.setEmail(user.getEmail());
	    	eUser.setPassword(passwordEncoder.encode(user.getPassword()));
	    	eUser.setMfaEnabled(user.isMfaEnabled());
//	    	eUser.setAddress(Address.builder().streetName(user.getStreet()).cityName(user.getCity()).state(State.valueOf(user.getState())).zipcode(Integer.valueOf(user.getZipCode())).country(user.getCountry()).build());
	    	userRepository.save(eUser);
	    	return ResponseEntity.ok("Profile updated success.");
    	}catch (Exception e) {
    		log.info("Update Failure :" + e.getMessage());
    		throw new UserNotFoundException("Update Failure : "+ e.getMessage());
    	}
    }
    

    public ResponseEntity<String> uploadProfilePicture(Integer userId, MultipartFile file) {
        // Check if the user exists in the database
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
    	
        
        try {
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }

            // Check if the uploaded file is empty
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Uploaded file is empty.");
            }

            // Check file size
            if (file.getSize() > maxFileSize) {
                return ResponseEntity.badRequest().body("File size exceeds the limit of " + (maxFileSize / (1024 * 1024)) + "MB.");
            }

            // Check file type
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = getFileExtension(originalFilename);

            List<String> allowedExtensionsList = Arrays.asList(allowedExtensions);
            if (!allowedExtensionsList.contains(fileExtension)) {
                return ResponseEntity.badRequest().body("Only " + Arrays.toString(allowedExtensions) + " files are allowed.");
            }
            
            // Generate a unique file name
            String fileName = generateUniqueFileName(file.getOriginalFilename());

            // Path where the file will be stored
            Path path = Paths.get(uploadProfileDirectory + fileName);

            // Save the file to the specified directory
            Files.write(path, file.getBytes());

            // Construct the file URL
            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(fileName)
                    .toUriString();

            // Update user profile picture URL in the database
            user.setProfilePicture(fileUrl);
            userRepository.save(user);

            return ResponseEntity.ok("File uploaded successfully.");
        } catch (IOException e) {
            log.error("Failed to upload file.", e);
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Failed to upload file.");
        }
    }

    private String getFileExtension(String fileName) {
        return StringUtils.getFilenameExtension(fileName).toLowerCase();
    }

    private String generateUniqueFileName(String originalFileName) {
        String uniqueID = UUID.randomUUID().toString();
        return uniqueID + "_" + originalFileName;
    }

	
}
