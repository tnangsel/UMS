package com.tenzin.services;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tenzin.datatypes.Permission;
import com.tenzin.datatypes.RoleType;
import com.tenzin.datatypes.State;
import com.tenzin.dto.RegisterRequest;
import com.tenzin.exceptions.EmailAlreadyExistsException;
import com.tenzin.models.Address;
import com.tenzin.models.Role;
import com.tenzin.models.User;
import com.tenzin.repository.UserRepository;


@Service
public class InitialSetUpService {

	private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
	
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    public void setupAdminUser() {
        Optional<User> user = userRepository.findByEmail("admin@gmail.com");
        if (user.isEmpty()) {
            Role adminRole = Role.builder()
                    .role(RoleType.ADMIN)
                    .permissions(Set.of(
                            Permission.ADMIN_READ,
                            Permission.ADMIN_UPDATE,
                            Permission.ADMIN_CREATE,
                            Permission.ADMIN_DELETE,
                            Permission.MANAGER_READ,
                            Permission.MANAGER_UPDATE,
                            Permission.MANAGER_CREATE,
                            Permission.MANAGER_DELETE,
                            Permission.USER_CREATE,
                            Permission.USER_READ,
                            Permission.USER_UPDATE,
                            Permission.USER_DELETE
                    ))
                    .createdDate(LocalDateTime.now())
                    .build();
            var address = Address.builder()
            		.streetName("1096 Raymond Ave")
            		.cityName("St. Paul")
            		.state(State.MN)
            		.zipcode(55108)
            		.country("USA")
            		.build();
            var adminUser = RegisterRequest.builder()
                    .firstname("admin")
                    .lastname("admin")
                    .email("admin@gmail.com")
                    .password("admin")
                    .role(adminRole.getRole())
                    .mfaEnabled(false)
                    .build();
            adminUser.setAddress(address);
            try {
            	
                logger.info("Admin Account: " + authService.register(adminUser));
            } catch (EmailAlreadyExistsException e) {
                e.printStackTrace();
            }
        }
    }
}

