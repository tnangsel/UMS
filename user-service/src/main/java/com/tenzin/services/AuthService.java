package com.tenzin.services;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenzin.datatypes.RoleType;
import com.tenzin.datatypes.TokenType;
import com.tenzin.dto.AuthenticationRequest;
import com.tenzin.dto.AuthenticationResponse;
import com.tenzin.dto.RegisterRequest;
import com.tenzin.dto.VerificationRequest;
import com.tenzin.exceptions.EmailAlreadyExistsException;
import com.tenzin.exceptions.InvalidOTPCodeException;
import com.tenzin.exceptions.InvalidTokenException;
import com.tenzin.exceptions.UserNotFoundException;
import com.tenzin.models.Role;
import com.tenzin.models.Token;
import com.tenzin.models.User;
import com.tenzin.repository.RoleRepository;
import com.tenzin.repository.TokenRepository;
import com.tenzin.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

@Service
public class AuthService {
    
	private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private TokenRepository tokenRepository;
	@Autowired
    private PasswordEncoder passwordEncoder;
	@Autowired
    private JwtService jwtService;
	@Autowired
    private AuthenticationManager authenticationManager;
	@Autowired
    private SMSService smsService;

	@Transactional
    public String register(RegisterRequest request) throws EmailAlreadyExistsException {
        // Check if the email already exists
        Optional<User> existUser = userRepository.findByEmail(request.getEmail());
		if (existUser.isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
		
        // Get or create the default role
        Role userRole = defaultUserRole(request.getRole());

        // Create new User object
        User user = User.builder()
                .firstName(request.getFirstname())
                .lastName(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .mfaEnabled(request.isMfaEnabled())
                .enabled(!request.isMfaEnabled()) // If MFA is enabled, set enabled to false
                .phoneNumber(request.getPhoneNumber())
                .createdDateTime(LocalDateTime.now())
                .lastModified(LocalDateTime.now())
                .roles(Set.of(userRole))
                .address(request.getAddress())
                .build();
        
        // Generate JWT token for the user
        String jwtToken = jwtService.generateToken(user);
        
        // If MFA is enabled, send OTP and set account to disabled
        if (request.isMfaEnabled()) {
        	String otp = generateOTP();
        	String secretOTP = passwordEncoder.encode(otp);
            user.setOTP(secretOTP);
            
            userRepository.save(user); // Update user with OTP
            generateTokenByUser(user, jwtToken);
            
            // Send OTP to the user's phone number
            smsService.sendSMS(user.getPhoneNumber(), "Your OTP is: " + otp);
	        logger.info("SMS text with OTP sent to: {}", user.getPhoneNumber());
	        return "Registration Successful and Multi factor enabled";
        }else {
        	// Save the user
            userRepository.save(user);
            generateTokenByUser(user, jwtToken);
            logger.info("Registration with {} successful", user.getEmail());
            return "Registration Successful";
        } 
    }

    // Method to get or create the default user role
    private Role defaultUserRole(RoleType roleType) {
        RoleType finalRoleType = roleType != null ? roleType : RoleType.USER;

        return roleRepository.findByRole(finalRoleType)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .role(finalRoleType)
                        .permissions(finalRoleType.getPermissions())
                        .createdDate(LocalDateTime.now())
                        .build()));
    }

    // Create and associate token with the user
    private Token generateTokenByUser(User user, String jwtToken) {
    	 Token token = Token.builder()
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .validatedAt(null)
                .expired(false)
                .revoked(false)
                .user(user)
                .build();
    	return tokenRepository.save(token);
    }
    
    // Generate random 6 digit OTP
    private String generateOTP() {
    	return new DecimalFormat("000000")
                .format(new Random().nextInt(999999));	
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) throws UserNotFoundException {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        
	        User user = userRepository.findByEmail(request.getEmail())
	            .orElseThrow(() -> new UserNotFoundException("Email does not exist."));
	
	        if (user.isMfaEnabled() && !user.isEnabled()) {
	            String otp = generateOTP();
	            String secretOtpCode = Base64.getEncoder().encodeToString(otp.getBytes());
	
	            user.setOTP(secretOtpCode);
	            userRepository.save(user);
	            smsService.sendSMS(user.getPhoneNumber(), otp);
	        }
	
	        user.setEnabled(true);
	        
	        List<Token> tokens = tokenRepository.findAllValidTokenByUserId(user.getId());
	        if(!tokens.isEmpty()) {
	        	tokens.stream().forEach((token) -> {
	        		token.setRevoked(true);
	        		token.setExpired(true);
	        		
	        	});
	        }
	        tokenRepository.saveAll(tokens);
	        
	        String jwtToken = jwtService.generateToken(user);
	        String refreshToken = jwtService.generateRefreshToken(user);
	        
	        Token token = generateTokenByUser(user, jwtToken);
	        token.setValidatedAt(LocalDateTime.now());
	        tokenRepository.save(token);
	        return AuthenticationResponse.builder()
	            .accessToken(jwtToken)
	            .refreshToken(refreshToken)
	            .mfaEnabled(false)
	            .build();
        }catch (AuthenticationException e) {
            throw new UserNotFoundException("Invalid email or password.");
        } 
    }

    
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws InvalidTokenException, IOException {
        
    	final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
       
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        	return;
        }
        
        final String refreshToken = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(refreshToken);
      
        if (userEmail != null) {
            User user = this.userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new InvalidTokenException("User not found for the provided email : " + userEmail));
            
            if (jwtService.isTokenValid(refreshToken, user)) {
                String accessToken = jwtService.generateToken(user);
                
                List<Token> tokens = tokenRepository.findAllValidTokenByUserId(user.getId());
                if(!tokens.isEmpty()) {
    	        	tokens.stream().forEach((token) -> {
    	        		if(!token.isRevoked() && !token.isExpired()) {
    	        			token.setToken(accessToken);
    	        			token.setValidatedAt(LocalDateTime.now());
    	        			tokenRepository.save(token);
    	        		}
    	        	});
    	        }
                
                AuthenticationResponse authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .mfaEnabled(false)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse.getAccessToken());
           
            } else {
            	throw new InvalidTokenException("Token is invalid");
            } 
        }
    }

    
    public String verifyOtpCode( VerificationRequest verificationRequest ) throws UserNotFoundException, InvalidOTPCodeException {
        User user = userRepository.findByEmail(verificationRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("No user found with %S", verificationRequest.getEmail()))
                );

        if (user.isEnabled()) {
            throw new InvalidOTPCodeException("Account is already activated. Please login");
        }
        
        // Decode the stored secret OTP
        boolean isOtpMatch = passwordEncoder.matches(verificationRequest.getCode(), user.getOTP());
        
        // Compare the provided OTP with the stored OTP
        if (isOtpMatch) {
            user.setEnabled(true);
            userRepository.save(user);
            logger.info("Account enabled successful");
            return "Your account is activated.";
        } else {
            throw new InvalidOTPCodeException("Invalid OTP code");
        }

    }
  
}
