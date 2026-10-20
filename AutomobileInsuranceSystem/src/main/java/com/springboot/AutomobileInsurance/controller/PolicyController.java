package com.springboot.AutomobileInsurance.controller;

import com.springboot.AutomobileInsurance.dto.response.PolicyResponseDto;
import com.springboot.AutomobileInsurance.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;
/*
    @GetMapping("/api/policy/number/{policyNumber}")
    public PolicyResponseDto getByPolicyNumber(@PathVariable String policyNumber) {
        return policyService.getByPolicyNumber(policyNumber);
    }

    @GetMapping("/api/policy/proposal/{proposalId}")
    public PolicyResponseDto getByProposalId(@PathVariable Long proposalId) {
        return policyService.getByProposalId(proposalId);
    }

    @GetMapping("/api/policy/all")
    public List<PolicyResponseDto> getAll() {
        return policyService.getAll();
    }

 */
}