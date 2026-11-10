package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.enums.Role;
import com.springboot.AutomobileInsurance.model.User;
import com.springboot.AutomobileInsurance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MyUserDetailServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MyUserDetailsService myUserDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(17L);
        user.setUsername("nina.patel");
        user.setPassword("encoded-nina-password");
        user.setRole(Role.CUSTOMER);
        user.setActive(true);
    }

    @Test
    void loadUserByUsername_returnsUserFromRepository() {
        when(userRepository.loadUserByUsername("nina.patel")).thenReturn(user);

        UserDetails result = myUserDetailsService.loadUserByUsername("nina.patel");

        assertSame(user, result);
        assertEquals("nina.patel", result.getUsername());
        assertEquals("encoded-nina-password", result.getPassword());
        assertEquals("CUSTOMER", result.getAuthorities().iterator().next().getAuthority());
        verify(userRepository).loadUserByUsername("nina.patel");
    }

    @Test
    void loadUserByUsername_propagatesExceptionWhenUserIsNotFound() {
        when(userRepository.loadUserByUsername("missing.user"))
                .thenThrow(new UsernameNotFoundException("User not found: missing.user"));

        assertThrows(
                UsernameNotFoundException.class,
                () -> myUserDetailsService.loadUserByUsername("missing.user")
        );

        verify(userRepository).loadUserByUsername("missing.user");
    }
}
