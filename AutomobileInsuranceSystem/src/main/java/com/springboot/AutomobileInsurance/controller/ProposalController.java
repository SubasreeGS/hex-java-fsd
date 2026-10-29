package com.springboot.AutomobileInsurance.controller;

import com.springboot.AutomobileInsurance.dto.request.ApproveIdvDto;
import com.springboot.AutomobileInsurance.dto.request.ProposalSubmissionDto;
import com.springboot.AutomobileInsurance.dto.response.ProposalInfoDto;
import com.springboot.AutomobileInsurance.dto.response.ProposalInfoDtoV2;
import com.springboot.AutomobileInsurance.dto.response.ProposalInfoDtoV3;
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

    //officer approves recommended idv-------------
    @PutMapping("/api/proposal/approve-idv/{proposalId}")
    public void approveIdv(@PathVariable Long proposalId,
                           @Valid @RequestBody ApproveIdvDto dto,Principal principal) {
        proposalService.approveIdv(proposalId, dto,principal.getName());
    }


    //get proposal by customer id (v1)
    @GetMapping("/api/proposal/customer/v1/{customerId}")
    public List<ProposalInfoDto> getProposalByCustomerId(@PathVariable Long customerId,
                                                         @RequestParam(name = "page",required = false,defaultValue = "0") int page,
                                                         @RequestParam(name ="size",required = false,defaultValue = "6")int size) {
        return proposalService.getProposalByCustomerId(customerId,page,size);
    }

    //get proposal by customer username (v2)
    @GetMapping("/api/proposal/customer/v2")
    public List<ProposalInfoDto> getByCustomerUsername(@RequestParam(name = "page",required = false,defaultValue = "0") int page,
                                                       @RequestParam(name ="size",required = false,defaultValue = "6")int size,
                                                       Principal principal)
    {
        return proposalService.getByCustomerUsername(page,size,principal.getName());

    }

    //get proposal by inspector username (v3)
    @GetMapping("/api/proposal/inspector")
    public List<ProposalInfoDtoV2> getByInspectorUsername(@RequestParam(name = "page",required = false,defaultValue = "0") int page,
                                                          @RequestParam(name ="size",required = false,defaultValue = "6")int size,
                                                          Principal principal)
    {
        return proposalService.getByInspectorUsername(page,size,principal.getName());
    }

    //get proposal by Officer username (v4)
    @GetMapping("/api/proposal/officer")
    public List<ProposalInfoDtoV3> getByOfficerUsername(@RequestParam(name = "page",required = false,defaultValue = "0") int page,
                                                        @RequestParam(name ="size",required = false,defaultValue = "6")int size,
                                                        Principal principal)
    {
        return proposalService.getByOfficerUsername(page,size,principal.getName());
    }


    //get proposal by status (v5)
    @GetMapping("/api/proposal/status/{status}")
    public List<ProposalInfoDtoV3> getByProposalStatus(@PathVariable ProposalStatus status,
                                                       @RequestParam(name = "page",required = false,defaultValue = "0") int page,
                                                       @RequestParam(name ="size",required = false,defaultValue = "10")int size,
                                                       Principal principal)
    {
        return proposalService.getByProposalStatus(status,page,size,principal.getName());
    }
/*
    @GetMapping("/api/proposal/officer/{officerId}")
    public List<ProposalResponseDto> getByOfficerId(@PathVariable long officerId) {
        return proposalService.getByOfficerId(officerId);
    }

    @GetMapping("/api/proposal/v1/{id}")
    public ProposalResponseDto getById(@PathVariable Long id) {
        return proposalService.getById(id);
    }


    @GetMapping("/api/proposal/all")
    public List<ProposalResponseDto> getAll() {
        return proposalService.getAll();
    }

 */
}