package com.springboot.AutomobileInsurance.mapper;

import com.springboot.AutomobileInsurance.dto.request.CustomerDto;
import com.springboot.AutomobileInsurance.dto.response.CustomerResponseDto;
import com.springboot.AutomobileInsurance.model.Customer;

public class CustomerMapper {

    public static Customer convertDtoToEntity(CustomerDto dto) {
        Customer customer = new Customer();
        customer.setName(dto.name());
        customer.setPhoneNo(dto.phoneNo());
        customer.setAddress(dto.address());
        customer.setDob(dto.dob());
        customer.setAadhaarNo(dto.aadhaarNo());
        customer.setPanNo(dto.panNo());
        return customer;
    }

    public static CustomerResponseDto convertEntityToDto(Customer customer) {
        return new CustomerResponseDto(
                customer.getId(),
                customer.getName(),
                maskPhone(customer.getPhoneNo()),
                customer.getAddress(),
                customer.getDob(),
                maskAadhaar(customer.getAadhaarNo()),
                maskPan(customer.getPanNo()),
                customer.getUser() != null ? customer.getUser().getId() : null,
                customer.getUser() != null ? customer.getUser().getUsername() : null
        );
    }

    // Mask Aadhaar: 123456789012 -> XXXXXXXX9012
    private static String maskAadhaar(String aadhaar) {
        if (aadhaar == null || aadhaar.length() < 4) {
            return aadhaar;
        }
        return "XXXXXXXX" + aadhaar.substring(aadhaar.length() - 4);
    }

    // Mask PAN: ABCDE1234F -> ABCDE****F
    private static String maskPan(String pan) {
        if (pan == null || pan.length() < 6) {
            return pan;
        }
        return pan.substring(0, 5) + "****" + pan.substring(pan.length() - 1);
    }

    // Optional Phone Mask: 9876543210 -> ******3210 (or keep raw if your frontend profile form edits it)
    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return phone;
        }
        return "******" + phone.substring(phone.length() - 4);
    }
}