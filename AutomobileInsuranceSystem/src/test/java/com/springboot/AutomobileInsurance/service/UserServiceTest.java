package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.AdminDto;
import com.springboot.AutomobileInsurance.dto.response.DetailsDto;
import com.springboot.AutomobileInsurance.enums.Role;
import com.springboot.AutomobileInsurance.exception.InactiveUserException;
import com.springboot.AutomobileInsurance.model.User;
import com.springboot.AutomobileInsurance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private AdminDto adminDto;
    private DetailsDto activeUserDetails;

    @BeforeEach
    void setUp() {
        adminDto = new AdminDto("sophia.admin", "SophiaAdmin!2026");
        activeUserDetails = new DetailsDto("sophia.admin", true, Role.ADMIN);
    }

    @Test
    void insertAdmin_encodesPasswordAndSavesAdminUser() {
        when(passwordEncoder.encode("SophiaAdmin!2026")).thenReturn("hashed-admin-password");

        userService.insertAdmin(adminDto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(passwordEncoder).encode("SophiaAdmin!2026");
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("sophia.admin", savedUser.getUsername());
        assertEquals("hashed-admin-password", savedUser.getPassword());
        assertEquals(Role.ADMIN, savedUser.getRole());
    }

    @Test
    void insertAdmin_propagatesPasswordEncoderFailure() {
        when(passwordEncoder.encode("SophiaAdmin!2026"))
                .thenThrow(new IllegalStateException("password encoder unavailable"));

        assertThrows(IllegalStateException.class, () -> userService.insertAdmin(adminDto));

        verify(passwordEncoder).encode("SophiaAdmin!2026");
        org.mockito.Mockito.verify(userRepository, org.mockito.Mockito.never()).save(any(User.class));
    }

    @Test
    void insertAdmin_propagatesRepositoryFailure() {
        when(passwordEncoder.encode("SophiaAdmin!2026")).thenReturn("hashed-admin-password");
        when(userRepository.save(any(User.class)))
                .thenThrow(new IllegalStateException("user could not be saved"));

        assertThrows(IllegalStateException.class, () -> userService.insertAdmin(adminDto));

        verify(userRepository).save(any(User.class));
    }

    @Test
    void getUserDetails_returnsDetailsForActiveUser() {
        when(userRepository.getUserDetails("sophia.admin")).thenReturn(activeUserDetails);

        DetailsDto response = userService.getUserDetails("sophia.admin");

        assertSame(activeUserDetails, response);
        assertEquals("sophia.admin", response.username());
        assertEquals(Role.ADMIN, response.role());
        verify(userRepository).getUserDetails("sophia.admin");
    }

    @Test
    void getUserDetails_throwsWhenUserIsInactive() {
        when(userRepository.getUserDetails("inactive.user"))
                .thenReturn(new DetailsDto("inactive.user", false, Role.CUSTOMER));

        assertThrows(InactiveUserException.class, () -> userService.getUserDetails("inactive.user"));

        verify(userRepository).getUserDetails("inactive.user");
    }

    @Test
    void getUserDetails_propagatesRepositoryFailure() {
        when(userRepository.getUserDetails("sophia.admin"))
                .thenThrow(new IllegalStateException("user lookup failed"));

        assertThrows(IllegalStateException.class, () -> userService.getUserDetails("sophia.admin"));

        verify(userRepository).getUserDetails("sophia.admin");
    }
}
