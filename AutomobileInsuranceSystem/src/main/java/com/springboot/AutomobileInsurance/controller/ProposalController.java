package com.springboot.AutomobileInsurance.controller;

import com.springboot.AutomobileInsurance.dto.request.ProposalSubmissionDto;
import com.springboot.AutomobileInsurance.dto.response.ProposalResponseDto;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;
import com.springboot.AutomobileInsurance.service.ProposalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor

public class ProposalController {

    private final ProposalService proposalService;

    @PostMapping("/api/proposal/submit")
    public ProposalResponseDto submitProposal(@Valid @RequestBody ProposalSubmissionDto dto) {
        return proposalService.submitProposal(dto);
    }

    @PutMapping("/api/proposal/assign/{inspectorId}/{proposalId}")
    public void assignInspector(@PathVariable Long inspectorId,
                                @PathVariable Long proposalId) {
        proposalService.assignInspector(inspectorId, proposalId);
    }

    @GetMapping("/api/proposal/{id}")
    public ProposalResponseDto getById(@PathVariable long id) {
        return proposalService.getById(id);
    }

    @GetMapping("/api/proposal/all")
    public List<ProposalResponseDto> getAll() {
        return proposalService.getAll();
    }

    @GetMapping("/api/proposal/customer/{customerId}")
    public List<ProposalResponseDto> getByCustomerId(@PathVariable long customerId) {
        return proposalService.getByCustomerId(customerId);
    }

    @GetMapping("/api/proposal/status/{status}")
    public List<ProposalResponseDto> getByStatus(@PathVariable ProposalStatus status) {
        return proposalService.getByStatus(status);
    }
}