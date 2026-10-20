package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.AdminDto;
import com.springboot.AutomobileInsurance.enums.Role;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.model.User;
import com.springboot.AutomobileInsurance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    // Prepare User using AdminDto
    public void insertAdmin(AdminDto dto) {
        User user = new User();
        user.setUsername(dto.username());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setRole(Role.ADMIN);

        // Save user in DB
        userRepository.save(user);

    }
}