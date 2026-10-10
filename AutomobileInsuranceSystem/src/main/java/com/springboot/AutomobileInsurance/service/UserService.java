package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.enums.Role;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.model.User;
import com.springboot.AutomobileInsurance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserObj(String username, String password, Role role) {
        if (userRepository.existsByUsername(username)) {
            throw new InvalidCallException("Username already exists: " + username);
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);

        return userRepository.save(user);
    }
}