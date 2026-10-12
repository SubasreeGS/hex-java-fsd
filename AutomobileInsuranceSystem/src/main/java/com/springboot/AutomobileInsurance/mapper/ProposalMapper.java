package com.springboot.AutomobileInsurance.mapper;

import com.springboot.AutomobileInsurance.dto.request.ProposalSubmissionDto;
import com.springboot.AutomobileInsurance.dto.response.ProposalResponseDto;
import com.springboot.AutomobileInsurance.model.Customer;
import com.springboot.AutomobileInsurance.model.Proposal;
import com.springboot.AutomobileInsurance.model.RtoVehicle;

public class ProposalMapper {

    public static Proposal convertDtoToEntity(ProposalSubmissionDto dto, Customer customer, RtoVehicle vehicle) {
        Proposal proposal = new Proposal();
        proposal.setCustomer(customer);
        proposal.setVehicle(vehicle);
        proposal.setRcDocUrl(dto.rcDocUrl());
        proposal.setPolicyType(dto.policyType());
        proposal.setZeroDep(dto.isZeroDep());
        proposal.setEngineProtect(dto.isEngineProtect());
        proposal.setRoadsideAssist(dto.isRoadsideAssist());
        return proposal;
    }

    public static ProposalResponseDto convertEntityToDto(Proposal proposal) {
        return new ProposalResponseDto(
                proposal.getId(),
                proposal.getCustomer().getId(),
                proposal.getCustomer().getName(),
                proposal.getVehicle().getId(),
                proposal.getVehicle().getRegistrationNo(),
                proposal.getVehicle().getMake(),
                proposal.getVehicle().getModel(),
                proposal.getVehicle().getManufactureYear(),
                proposal.getOfficer() != null ? proposal.getOfficer().getId() : null,
                proposal.getOfficer() != null ? proposal.getOfficer().getName() : null,
                proposal.getRcDocUrl(),
                proposal.getPolicyType(),
                proposal.isZeroDep(),
                proposal.isEngineProtect(),
                proposal.isRoadsideAssist(),
                proposal.getStatus()
        );
    }
}