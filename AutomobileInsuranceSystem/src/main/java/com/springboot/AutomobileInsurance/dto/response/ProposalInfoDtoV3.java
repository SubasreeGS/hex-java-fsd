package com.springboot.AutomobileInsurance.dto.response;

import com.springboot.AutomobileInsurance.enums.FuelType;
import com.springboot.AutomobileInsurance.enums.PolicyType;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;

import java.time.LocalDate;

public record ProposalInfoDtoV3(
        Long id,
        ProposalStatus status,
        PolicyType policyType,
        boolean isZeroDep,
        boolean isEngineProtect,
        boolean isRoadsideAssist,

        String name,
        String phoneNo,
        String address,
        LocalDate dob,
        String aadhaarNo,
        String panNo,

        String registrationNo,
        String vehicleMake,
        String vehicleModel,
        Integer manufactureYear,
        FuelType fuelType,
        String rcDocUrl,
        String vinNo,
        Integer engineCapacity,

        String inspectorName

) {
}
