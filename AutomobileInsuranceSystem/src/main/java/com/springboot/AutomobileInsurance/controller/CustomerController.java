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

public class CustomerController {

    private final CustomerService customerService;

    //add new customer
    @PostMapping("/api/customer/add")
    public void insertCustomer(@Valid @RequestBody CustomerDto customerDto) {
        customerService.insertCustomer(customerDto);
    }

    //get customer by customer id
    @GetMapping("/api/customer/{id}")
    public CustomerResponseDto getById(@PathVariable Long id) {

        return customerService.getById(id);
    }

    //get all customers as list of dto
    @GetMapping("/api/customer/all")
    public List<CustomerResponseDto> getAll() {

        return customerService.getAll();
    }

    // delete customer
    @DeleteMapping("/api/customer/{id}")
    public void deleteById(@PathVariable Long id)
    {
        customerService.deleteById(id);
    }

    //update customer by id
    @PutMapping("/api/customer/{id}")
    public void update(@PathVariable Long id, @RequestBody CustomerUpdateDto dto) {
        customerService.update(id, dto);
    }
}