package com.springboot.AutomobileInsurance.controller;

import com.springboot.AutomobileInsurance.dto.request.ApproveIdvDto;
import com.springboot.AutomobileInsurance.dto.request.ProposalSubmissionDto;
import com.springboot.AutomobileInsurance.dto.response.ProposalResponseDto;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;
import com.springboot.AutomobileInsurance.service.ProposalService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Transactional
public class ProposalController {

    private final ProposalService proposalService;

    // customer submits new proposal
    @PostMapping("/api/proposal/submit")
    public void submitProposal(@Valid @RequestBody ProposalSubmissionDto dto,Principal principal) {
         proposalService.submitProposal(dto,principal.getName());
    }

    //officer assigns inspector to proposal
    @PutMapping("/api/proposal/assign/{inspectorId}/{proposalId}")
    public void assignInspector(@PathVariable Long inspectorId,
                                @PathVariable Long proposalId, Principal principal) {
        proposalService.assignInspector(inspectorId, proposalId,principal.getName());
    }

    //officer approves recommended idv
    @PutMapping("/api/proposal/approve-idv/{proposalId}")
    public void approveIdv(@PathVariable Long proposalId,
                           @Valid @RequestBody ApproveIdvDto dto,Principal principal) {
        proposalService.approveIdv(proposalId, dto,principal.getName());
    }

    @GetMapping("/api/proposal/all")
    public List<ProposalResponseDto> getAll() {
        return proposalService.getAll();
    }

    @GetMapping("/api/proposal/{id}")
    public ProposalResponseDto getById(@PathVariable long id) {
        return proposalService.getById(id);
    }

    @GetMapping("/api/proposal/customer/{customerId}")
    public List<ProposalResponseDto> getByCustomerId(@PathVariable long customerId) {
        return proposalService.getByCustomerId(customerId);
    }

    @GetMapping("/api/proposal/status/{status}")
    public List<ProposalResponseDto> getByStatus(@PathVariable ProposalStatus status) {
        return proposalService.getByStatus(status);
    }

    @GetMapping("/api/proposal/officer/{officerId}")
    public List<ProposalResponseDto> getByOfficerId(@PathVariable long officerId) {
        return proposalService.getByOfficerId(officerId);
    }
}