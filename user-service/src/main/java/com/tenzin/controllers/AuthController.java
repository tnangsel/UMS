package com.tenzin.controllers;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tenzin.dto.AuthenticationRequest;
import com.tenzin.dto.AuthenticationResponse;
import com.tenzin.dto.RegisterRequest;
import com.tenzin.dto.VerificationRequest;
import com.tenzin.exceptions.EmailAlreadyExistsException;
import com.tenzin.exceptions.InvalidOTPCodeException;
import com.tenzin.exceptions.InvalidTokenException;
import com.tenzin.exceptions.MfaEnabledException;
import com.tenzin.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	@Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) throws EmailAlreadyExistsException, MfaEnabledException {
        String response = authService.register(request);
        if (request.isMfaEnabled()) {
            return ResponseEntity.status(HttpStatus.SC_LOCKED).body(response);
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/refresh-token")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws InvalidTokenException, IOException {
        authService.refreshToken(request, response);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> validateUserCode(@Valid @RequestBody VerificationRequest verificationRequest) throws InvalidOTPCodeException{
        return ResponseEntity.ok(authService.verifyOtpCode(verificationRequest));
    }
    
}
