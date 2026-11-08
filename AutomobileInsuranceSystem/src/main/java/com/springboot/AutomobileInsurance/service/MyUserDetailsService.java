package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class MyUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final Logger log = LoggerFactory.getLogger(ClaimService.class);

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        return userRepository.loadUserByUsername(username);

    }
}
