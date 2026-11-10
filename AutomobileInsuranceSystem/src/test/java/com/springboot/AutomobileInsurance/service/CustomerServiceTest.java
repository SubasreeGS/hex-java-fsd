package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.CustomerDto;
import com.springboot.AutomobileInsurance.dto.request.CustomerUpdateDto;
import com.springboot.AutomobileInsurance.dto.response.CustomerResponseDto;
import com.springboot.AutomobileInsurance.enums.Role;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.model.Customer;
import com.springboot.AutomobileInsurance.model.User;
import com.springboot.AutomobileInsurance.repository.CustomerRepository;
import com.springboot.AutomobileInsurance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerService customerService;

    private CustomerDto customerDto;
    private Customer customer;
    private User user;

    @BeforeEach
    void setUp() {
        customerDto = new CustomerDto(
                "maya.sharma",
                "MayaSecure!42",
                "Maya Sharma",
                "9876543210",
                "24 Lakeview Road, Pune",
                LocalDate.of(1992, 4, 18),
                "123456789012",
                "ABCDE1234F"
        );

        user = new User();
        user.setUsername("maya.sharma");
        user.setPassword("encoded-password");
        user.setRole(Role.CUSTOMER);

        customer = new Customer();
        customer.setId(42L);
        customer.setName("Maya Sharma");
        customer.setPhoneNo("9876543210");
        customer.setAddress("24 Lakeview Road, Pune");
        customer.setDob(LocalDate.of(1992, 4, 18));
        customer.setAadhaarNo("123456789012");
        customer.setPanNo("ABCDE1234F");
        customer.setUser(user);
    }

    @Test
    void insertCustomer_savesUserAndCustomer() {
        when(userRepository.existsByUsername("maya.sharma")).thenReturn(false);
        when(passwordEncoder.encode("MayaSecure!42")).thenReturn("hashed-MayaSecure");

        customerService.insertCustomer(customerDto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(userRepository).save(userCaptor.capture());
        verify(customerRepository).save(customerCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("maya.sharma", savedUser.getUsername());
        assertEquals("hashed-MayaSecure", savedUser.getPassword());
        assertEquals(Role.CUSTOMER, savedUser.getRole());

        Customer savedCustomer = customerCaptor.getValue();
        assertEquals("Maya Sharma", savedCustomer.getName());
        assertEquals("9876543210", savedCustomer.getPhoneNo());
        assertEquals("24 Lakeview Road, Pune", savedCustomer.getAddress());
        assertEquals("123456789012", savedCustomer.getAadhaarNo());
        assertEquals("ABCDE1234F", savedCustomer.getPanNo());
        assertEquals(savedUser, savedCustomer.getUser());
    }

    @Test
    void insertCustomer_throwsWhenUsernameAlreadyExists() {
        when(userRepository.existsByUsername("maya.sharma")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> customerService.insertCustomer(customerDto));

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
        verifyNoInteractions(passwordEncoder, customerRepository);
    }

    @Test
    void getById_returnsCustomerResponse() {
        when(customerRepository.findById(42L)).thenReturn(Optional.of(customer));

        CustomerResponseDto response = customerService.getById(42L);

        assertEquals(42L, response.id());
        assertEquals("Maya Sharma", response.name());
        assertEquals("maya.sharma", response.username());
        assertEquals(LocalDate.of(1992, 4, 18), response.dob());
    }

    @Test
    void getById_throwsWhenCustomerDoesNotExist() {
        when(customerRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.getById(404L));
    }

    @Test
    void getByCustomerUsername_returnsCustomerResponse() {
        when(customerRepository.getByCustomerUsername("maya.sharma")).thenReturn(customer);

        CustomerResponseDto response = customerService.getByCustomerUsername("maya.sharma");

        assertEquals("Maya Sharma", response.name());
        assertEquals("maya.sharma", response.username());
    }

    @Test
    void getAll_returnsCustomerResponses() {
        Customer anotherCustomer = new Customer();
        anotherCustomer.setId(43L);
        anotherCustomer.setName("Arjun Mehta");
        anotherCustomer.setPhoneNo("9123456780");
        anotherCustomer.setAddress("8 Green Park, Mumbai");
        anotherCustomer.setDob(LocalDate.of(1988, 9, 3));
        anotherCustomer.setUser(user);
        when(customerRepository.findAll()).thenReturn(List.of(customer, anotherCustomer));

        List<CustomerResponseDto> responses = customerService.getAll();

        assertEquals(2, responses.size());
        assertEquals("Maya Sharma", responses.get(0).name());
        assertEquals("Arjun Mehta", responses.get(1).name());
    }

    @Test
    void deleteById_deletesExistingCustomer() {
        when(customerRepository.findById(42L)).thenReturn(Optional.of(customer));

        customerService.deleteById(42L);

        verify(customerRepository).deleteById(42L);
    }

    @Test
    void deleteById_throwsAndDoesNotDeleteMissingCustomer() {
        when(customerRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.deleteById(404L));

        verify(customerRepository, never()).deleteById(404L);
    }

    @Test
    void update_changesProvidedFieldsAndSavesCustomer() {
        CustomerUpdateDto updateDto = new CustomerUpdateDto(
                "Maya S. Sharma",
                "9876501234",
                "18 Riverside Avenue, Pune",
                LocalDate.of(1992, 4, 19)
        );
        when(customerRepository.findById(42L)).thenReturn(Optional.of(customer));

        customerService.update(42L, updateDto);

        verify(customerRepository).save(customer);
        assertEquals("Maya S. Sharma", customer.getName());
        assertEquals("9876501234", customer.getPhoneNo());
        assertEquals("18 Riverside Avenue, Pune", customer.getAddress());
        assertEquals(LocalDate.of(1992, 4, 19), customer.getDob());
    }

    @Test
    void update_throwsWhenCustomerDoesNotExist() {
        when(customerRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> customerService.update(404L, new CustomerUpdateDto("New Name", null, null, null))
        );

        verify(customerRepository, never()).save(org.mockito.ArgumentMatchers.any(Customer.class));
    }

    @Test
    void update_throwsWhenNoUsableFieldsAreProvided() {
        when(customerRepository.findById(42L)).thenReturn(Optional.of(customer));

        assertThrows(
                InvalidCallException.class,
                () -> customerService.update(42L, new CustomerUpdateDto("  ", null, null, null))
        );

        verify(customerRepository, never()).save(org.mockito.ArgumentMatchers.any(Customer.class));
    }

    @Test
    void updateByUsername_changesProvidedFieldsAndSavesCustomer() {
        when(customerRepository.getByCustomerUsername("maya.sharma")).thenReturn(customer);

        customerService.updateByUsername(
                "maya.sharma",
                new CustomerUpdateDto("Maya S. Sharma", null, "18 Riverside Avenue, Pune", null)
        );

        verify(customerRepository).save(customer);
        assertEquals("Maya S. Sharma", customer.getName());
        assertEquals("18 Riverside Avenue, Pune", customer.getAddress());
        assertNotNull(customer.getUser());
    }

    @Test
    void updateByUsername_throwsWhenNoUsableFieldsAreProvided() {
        when(customerRepository.getByCustomerUsername("maya.sharma")).thenReturn(customer);

        assertThrows(
                InvalidCallException.class,
                () -> customerService.updateByUsername(
                        "maya.sharma",
                        new CustomerUpdateDto(null, "   ", null, null)
                )
        );

        verify(customerRepository, never()).save(org.mockito.ArgumentMatchers.any(Customer.class));
    }
}
