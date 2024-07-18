package com.tenzin.services;

import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import com.tenzin.models.Token;
import com.tenzin.repository.TokenRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class LogoutService implements LogoutHandler{
	
	@Autowired
	private TokenRepository tokenRepository;
	
	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		final String jwtToken;
		if(authHeader == null || !authHeader.startsWith("Bearer ")) {
			return;
		}
		jwtToken = authHeader.substring(7);
		
		Token storedToken = tokenRepository.findByToken(jwtToken).orElse(null);
		if(storedToken != null) {
			storedToken.setExpired(true);
			storedToken.setRevoked(true);
			tokenRepository.save(storedToken);
		}
	}

}
