package com.tenzin.configurations;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.tenzin.datatypes.Permission;
import com.tenzin.datatypes.RoleType;
import com.tenzin.services.LogoutService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {
	
	@Autowired
	private JwtAuthFilter jwtAuthFilter;
	@Autowired
	private AuthenticationProvider authenticationProvider;
	@Autowired
	private LogoutService logoutService;
	
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.cors(Customizer.withDefaults())
				.csrf(Csrf -> Csrf.disable())
				.authorizeHttpRequests(auth -> auth
        			.requestMatchers("/api/v1/auth/**")
        				.permitAll()
        				
        			.requestMatchers("/api/v1/admin/**").hasAnyRole(RoleType.ADMIN.name())
                    .requestMatchers(GET, "/api/v1/admin/**").hasAnyAuthority(Permission.ADMIN_READ.name())
                    .requestMatchers(POST, "/api/v1/admin/**").hasAnyAuthority(Permission.ADMIN_CREATE.name())
                    .requestMatchers(PUT, "/api/v1/admin/**").hasAnyAuthority(Permission.ADMIN_UPDATE.name())
                    .requestMatchers(DELETE, "/api/v1/admin/**").hasAnyAuthority(Permission.ADMIN_DELETE.name())
		            
                    .requestMatchers("/api/v1/managment/**").hasAnyRole(RoleType.ADMIN.name(), RoleType.MANAGER.name())
                    .requestMatchers(GET, "/api/v1/managment/**").hasAnyAuthority(Permission.ADMIN_READ.name(), Permission.MANAGER_READ.name())
                    .requestMatchers(POST, "/api/v1/managment/**").hasAnyAuthority(Permission.ADMIN_CREATE.name(), Permission.MANAGER_CREATE.name())
                    .requestMatchers(PUT, "/api/v1/managment/**").hasAnyAuthority(Permission.ADMIN_UPDATE.name(), Permission.MANAGER_UPDATE.name())
                    .requestMatchers(DELETE, "/api/v1/managment/**").hasAnyAuthority(Permission.ADMIN_DELETE.name(), Permission.MANAGER_DELETE.name())
		            
                    .requestMatchers("/api/v1/user/**").hasAnyRole(RoleType.USER.name(), RoleType.MANAGER.name(), RoleType.ADMIN.name())
                    .requestMatchers(GET, "/api/v1/user/**").hasAnyAuthority(Permission.USER_READ.name(), Permission.ADMIN_READ.name(), Permission.MANAGER_READ.name())
                    .requestMatchers(POST, "/api/v1/user/**").hasAnyAuthority(Permission.USER_CREATE.name(), Permission.ADMIN_CREATE.name(), Permission.MANAGER_CREATE.name())
                    .requestMatchers(PUT, "/api/v1/user/**").hasAnyAuthority(Permission.USER_UPDATE.name(), Permission.ADMIN_UPDATE.name(), Permission.MANAGER_UPDATE.name())
                    .requestMatchers(DELETE, "/api/v1/user/**").hasAnyAuthority(Permission.USER_DELETE.name(), Permission.ADMIN_DELETE.name(), Permission.MANAGER_DELETE.name())
		            
                    .anyRequest()
		            .authenticated())
	        	.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	            .authenticationProvider(authenticationProvider)
	            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
	            .logout(logout -> logout.logoutUrl("/api/v1/auth/logout")
	            						.addLogoutHandler(logoutService)
	            						.logoutSuccessHandler((auth, response, authenticate) -> SecurityContextHolder.clearContext()))
	            .build();
		
	}
	
}
