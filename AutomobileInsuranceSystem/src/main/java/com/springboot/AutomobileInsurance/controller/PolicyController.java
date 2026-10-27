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

    //get policy by policy number
    @GetMapping("/api/policy/number/v1/{policyNumber}")
    public PolicyResponseDto getByPolicyNumber(@PathVariable String policyNumber) {
        return policyService.getByPolicyNumber(policyNumber);
    }

    //get by quote id
    @GetMapping("/api/policy/quote/v2/{quoteId}")
    public PolicyResponseDto getByQuoteId(@PathVariable Long quoteId) {
        return policyService.getByQuoteId(quoteId);
    }

    //get by proposal id
    @GetMapping("/api/policy/proposal/v3/{proposalId}")
    public PolicyResponseDto getByProposalId(@PathVariable Long proposalId) {
        return policyService.getByProposalId(proposalId);
    }

    //get all policies
    @GetMapping("/api/policy/v4/all")
    public List<PolicyResponseDto> getAll() {
        return policyService.getAll();
    }


}