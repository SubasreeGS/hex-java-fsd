package com.springboot.AutomobileInsurance.controller;

import com.springboot.AutomobileInsurance.dto.request.CustomerDto;
import com.springboot.AutomobileInsurance.dto.request.CustomerUpdateDto;
import com.springboot.AutomobileInsurance.dto.response.CustomerResponseDto;
import com.springboot.AutomobileInsurance.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/api/customer/add")
    public void insertCustomer(@Valid @RequestBody CustomerDto customerDto) {
        customerService.insertCustomer(customerDto);
    }

    @GetMapping("/api/customer/{id}")
    public CustomerResponseDto getById(@PathVariable long id) {
        return customerService.getById(id);
    }

    @GetMapping("/api/customer/all")
    public List<CustomerResponseDto> getAll() {
        return customerService.getAll();
    }

    @DeleteMapping("/api/customer/{id}")
    public void deleteById(@PathVariable long id) {
        customerService.deleteById(id);
    }

    @PutMapping("/api/customer/{id}")
    public void update(@PathVariable Long id, @RequestBody CustomerUpdateDto dto) {
        customerService.update(id, dto);
    }
}