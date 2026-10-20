package com.springboot.AutomobileInsurance.dto.response;

import com.springboot.AutomobileInsurance.enums.FuelType;
import com.springboot.AutomobileInsurance.enums.PolicyType;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;

public record ProposalInfoDtoV2(
        Long id,
        String customerName,
        String customerPhoneNo,
        String customerAddress,

        String registrationNo,
        String make,
        String model,
        Integer manufactureYear,
        Integer engineCapacity,
        String vinNo,
        FuelType fuelType,

        String rcDocUrl,
        PolicyType policyType,
        boolean isZeroDep,
        boolean isEngineProtect,
        boolean isRoadsideAssist
) {
}
