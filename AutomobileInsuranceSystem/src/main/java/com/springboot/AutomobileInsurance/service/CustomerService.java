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
import com.springboot.AutomobileInsurance.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

//add new customer
    public void insertCustomer(CustomerDto dto) {
        //prepare user
        User user = new User();
        user.setUsername(dto.username());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setRole(Role.CUSTOMER);
        //save user to user db
        userRepository.save(user);

        //convert dto to entity
        Customer customer = CustomerMapper.convertDtoToEntity(dto);
        //inject user into customer
        customer.setUser(user);
        //save customer in db
        customerRepository.save(customer);
    }
//get customer by customer id
    public CustomerResponseDto getById(Long id) {
       Customer customer = customerRepository.findById(id)
               .orElseThrow(()-> new ResourceNotFoundException("Invalid customer id"));

        return CustomerMapper.convertEntityToDto(customer);
    }

    public CustomerResponseDto getByCustomerUsername(String name) {
       Customer customer = customerRepository.getByCustomerUsername(name);
       return CustomerMapper.convertEntityToDto(customer);

    }

    //get all customers as list of dto
    public List<CustomerResponseDto> getAll() {
        return customerRepository.findAll()
                .stream()
                .map(CustomerMapper::convertEntityToDto)
                .toList();
    }
    // delete customer
    public void deleteById(Long id) {
        getById(id);
        customerRepository.deleteById(id);
    }

    //update customer by id
    public void update(Long id, CustomerUpdateDto dto) {
        //check customer id and get its object
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid customer id"));

        //to check whether any attribute got updated
        boolean flag = false;
        //update name if given
        if (dto.name() != null && !dto.name().trim().isEmpty()) {
            customer.setName(dto.name());
            flag = true;
        }
        //update phone number if given
        if (dto.phoneNo() != null && !dto.phoneNo().trim().isEmpty()) {
            customer.setPhoneNo(dto.phoneNo());
            flag = true;
        }
        //update address if given
        if (dto.address() != null && !dto.address().trim().isEmpty()) {
            customer.setAddress(dto.address());
            flag = true;
        }
        //update dob if given
        if (dto.dob() != null) {
            customer.setDob(dto.dob());
            flag = true;
        }
        //check whether update is done
        if (!flag) {
            throw new InvalidCallException("Update operation terminated");
        }

        customerRepository.save(customer);
    }

    // Update logged-in customer's profile
    public void updateByUsername(String username, CustomerUpdateDto dto) {

        Customer customer = customerRepository.getByCustomerUsername(username);

        //to check whether any attribute got updated
        boolean flag = false;
        //update name if given
        if (dto.name() != null && !dto.name().trim().isEmpty()) {
            customer.setName(dto.name());
            flag = true;
        }
        //update phone number if given
        if (dto.phoneNo() != null && !dto.phoneNo().trim().isEmpty()) {
            customer.setPhoneNo(dto.phoneNo());
            flag = true;
        }
        //update address if given
        if (dto.address() != null && !dto.address().trim().isEmpty()) {
            customer.setAddress(dto.address());
            flag = true;
        }
        //update dob if given
        if (dto.dob() != null) {
            customer.setDob(dto.dob());
            flag = true;
        }
        //check whether update is done
        if (!flag) {
            throw new InvalidCallException("Update operation terminated");
        }

        customerRepository.save(customer);
    }
}