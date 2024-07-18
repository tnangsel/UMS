package com.tenzin.services;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	@Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${security.jwt.refresh-token.expiration}")
    private long refreshExpiration;
	
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}
	
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
	    return claimsResolver.apply(claims);
	}
	
	private Claims extractAllClaims(String token) {
	    return Jwts
	        .parser()
	        .verifyWith((SecretKey)getSignInKey())
	        .build()
	        .parseSignedClaims(token)
	        .getPayload();
 
	}
	
	public String generateToken(UserDetails userDetails) {
	    return generateToken(new HashMap<>(), userDetails);
	}
	
	public String generateToken(Map<String, Object> extractClaims, UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
	    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
	    claims.put("authorities", authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));    
		return buildToken(extractClaims, userDetails, jwtExpiration);
	}
	
	public String generateRefreshToken(UserDetails userDetails) {
	    Map<String, Object> claims = new HashMap<>();
	    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
	    claims.put("authorities", authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
	    return buildToken(claims, userDetails, refreshExpiration);
	}
	
//	public String generateRefreshToken(UserDetails userDetails) {
//		return buildToken(new HashMap<>(), userDetails, refreshExpiration);
//	}
	
	public String buildToken(Map<String, Object> extractClaims, UserDetails userDetails, long expiration ) {
	    return Jwts.builder()
	            .claims(extractClaims)
	            .subject(userDetails.getUsername())
	            .issuedAt(new Date(System.currentTimeMillis()))
	            .expiration(new Date(System.currentTimeMillis() + expiration))
	            .signWith(getSignInKey())
	            .compact();
	}
	
	public boolean isTokenValid(String token, UserDetails userDetails) {
	    final String username = extractUsername(token);
	    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
	}
	
	private boolean isTokenExpired(String token){
	    return extractExpiration(token).before(new Date());
	}
	
	private Date extractExpiration(String token) {
	    return extractClaim(token, Claims::getExpiration);
	}
	
	
	private Key getSignInKey() {
	    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
	    return Keys.hmacShaKeyFor(keyBytes);
	}
}
