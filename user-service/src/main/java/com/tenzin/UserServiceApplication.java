package com.tenzin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.tenzin.services.InitialSetUpService;

@SpringBootApplication
public class UserServiceApplication {
	
	@Autowired
	private InitialSetUpService initialSetUpService;
	
	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}
	
	@Bean
	CommandLineRunner runner() {
		return args -> {
			initialSetUpService.setupAdminUser();
		};
	}

}
