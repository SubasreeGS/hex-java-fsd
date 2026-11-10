package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.AdminDto;
import com.springboot.AutomobileInsurance.dto.response.DetailsDto;
import com.springboot.AutomobileInsurance.enums.Role;
import com.springboot.AutomobileInsurance.exception.InactiveUserException;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.model.User;
import com.springboot.AutomobileInsurance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    // Prepare User using AdminDto
    @Transactional
    public void insertAdmin(AdminDto dto) {
        User user = new User();
        user.setUsername(dto.username());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setRole(Role.ADMIN);

        // Save user in DB
        userRepository.save(user);
        log.info("Admin user created: {}", dto.username());
    }

    public DetailsDto getUserDetails(String loggedInUsername) {
        DetailsDto detailsDto = userRepository.getUserDetails(loggedInUsername);
        if(!detailsDto.isActive()) {
            throw new InactiveUserException("User is disabled");
        }
        log.info("Fetching details for user: {}", loggedInUsername);
        return detailsDto;
    }

}