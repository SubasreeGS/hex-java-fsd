package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.CustomerDto;
import com.springboot.AutomobileInsurance.dto.request.CustomerUpdateDto;
import com.springboot.AutomobileInsurance.dto.response.CustomerResponseDto;
import com.springboot.AutomobileInsurance.enums.Role;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.mapper.CustomerMapper;
import com.springboot.AutomobileInsurance.model.Customer;
import com.springboot.AutomobileInsurance.model.User;
import com.springboot.AutomobileInsurance.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserService userService;

    public void insertCustomer(CustomerDto dto) {
        User user = userService.getUserObj(
                dto.username(),
                dto.password(),
                Role.CUSTOMER
        );

        Customer customer = CustomerMapper.convertDtoToEntity(dto);
        customer.setUser(user);
        customerRepository.save(customer);
    }

    public CustomerResponseDto getById(long id) {
        Optional<Customer> optional = customerRepository.findById(id);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Invalid customer id");
        }
        return CustomerMapper.convertEntityToDto(optional.get());
    }

    public List<CustomerResponseDto> getAll() {
        return customerRepository.findAll()
                .stream()
                .map(CustomerMapper::convertEntityToDto)
                .toList();
    }

    public void deleteById(long id) {
        getById(id);
        customerRepository.deleteById(id);
    }

    public void update(Long id, CustomerUpdateDto dto) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid customer id"));

        boolean flag = false;

        if (dto.name() != null && !dto.name().trim().isEmpty()) {
            customer.setName(dto.name());
            flag = true;
        }

        if (dto.phoneNo() != null && !dto.phoneNo().trim().isEmpty()) {
            customer.setPhoneNo(dto.phoneNo());
            flag = true;
        }

        if (dto.address() != null && !dto.address().trim().isEmpty()) {
            customer.setAddress(dto.address());
            flag = true;
        }

        if (dto.dob() != null) {
            customer.setDob(dto.dob());
            flag = true;
        }

        if (!flag) {
            throw new InvalidCallException("Update op terminated");
        }

        customerRepository.save(customer);
    }
}