package com.springboot.AutomobileInsurance.mapper;

import com.springboot.AutomobileInsurance.dto.request.CustomerDto;
import com.springboot.AutomobileInsurance.dto.response.CustomerResponseDto;
import com.springboot.AutomobileInsurance.model.Customer;
//convert Dto to Entity
public class CustomerMapper {

    public static Customer convertDtoToEntity(CustomerDto dto)
    {
        Customer customer = new Customer();
        customer.setName(dto.name());
        customer.setPhoneNo(dto.phoneNo());
        customer.setAddress(dto.address());
        customer.setDob(dto.dob());
        customer.setAadhaarNo(dto.aadhaarNo());
        customer.setPanNo(dto.panNo());
        return customer;
    }

    //Convert Entity to Dto
    public static CustomerResponseDto convertEntityToDto(Customer customer)
    {
        return new CustomerResponseDto(
                customer.getId(),
                customer.getName(),
                customer.getPhoneNo(),
                customer.getAddress(),
                customer.getDob(),
                customer.getUser().getUsername()
        );
    }

}