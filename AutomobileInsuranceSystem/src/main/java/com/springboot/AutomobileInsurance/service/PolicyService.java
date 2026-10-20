package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.response.PolicyResponseDto;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.mapper.PolicyMapper;
import com.springboot.AutomobileInsurance.model.Policy;
import com.springboot.AutomobileInsurance.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;
/*
    public PolicyResponseDto getByPolicyNumber(String policyNumber) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with policy number: " + policyNumber));
        return PolicyMapper.convertEntityToDto(policy);
    }

    public PolicyResponseDto getByProposalId(Long proposalId) {
        Policy policy = policyRepository.findByProposalId(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found for proposal id: " + proposalId));
        return PolicyMapper.convertEntityToDto(policy);
    }

    public List<PolicyResponseDto> getAll() {
        return policyRepository.findAll()
                .stream()
                .map(PolicyMapper::convertEntityToDto)
                .toList();
    }

 */
}