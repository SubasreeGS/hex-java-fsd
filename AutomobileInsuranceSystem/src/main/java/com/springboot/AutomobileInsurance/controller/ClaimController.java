package com.springboot.AutomobileInsurance.controller;

import com.springboot.AutomobileInsurance.dto.request.ClaimFileRequestDto;
import com.springboot.AutomobileInsurance.dto.request.ClaimReviewRequestDto;
import com.springboot.AutomobileInsurance.dto.response.ClaimResponseDto;
import com.springboot.AutomobileInsurance.dto.response.ClaimResponseDtoV2;
import com.springboot.AutomobileInsurance.dto.response.ClaimResponseV2PaginationDto;
import com.springboot.AutomobileInsurance.dto.response.ClaimSettlementReceiptDto;
import com.springboot.AutomobileInsurance.service.ClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")

public class ClaimController {

    private final ClaimService claimService;

    // File a new claim
    @PostMapping("/api/claim/file")
    public void fileClaim(@Valid @RequestBody ClaimFileRequestDto request) {
         claimService.fileClaim(request);
    }

    // Dedicated Officer Ledger Endpoint
    @GetMapping("/api/claim/officer/all")
    public ClaimResponseV2PaginationDto getAllClaimsForOfficer(
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "5") int size) {
        return claimService.getAllClaimsForOfficer(page, size);
    }

    // Officer assigns an inspector to investigate a filed claim
    @PutMapping("/api/claim/assign/{inspectorId}/{claimId}")
    public void assignInspectorToClaim(
            @PathVariable Long inspectorId,
            @PathVariable Long claimId,
            Principal principal) {
        claimService.assignInspector(inspectorId, claimId, principal.getName());
    }

    // Customer Dashboard - View all claims filed by the logged-in customer
    @GetMapping("/api/claim/customer/my-claims/v4")
    public List<ClaimResponseDto> getMyClaims(Principal principal) {
        return claimService.getClaimsByCustomerUsername(principal.getName());
    }

    // Review, approve, or reject claim by the assigned officer
    @PutMapping("/api/claim/review/{claimId}")
    public void reviewClaim(
            @PathVariable Long claimId,
            @Valid @RequestBody ClaimReviewRequestDto request
           ) {
        claimService.reviewClaim(claimId, request);
    }

    // Get claim by ID
    @GetMapping("/api/claim/id/v1/{id}")
    public ClaimResponseDto getById(@PathVariable Long id) {
        return claimService.getById(id);
    }

    // Get claim by claim number
    @GetMapping("/api/claim/number/v2/{claimNo}")
    public ClaimResponseDto getByClaimNo(@PathVariable String claimNo) {
        return claimService.getByClaimNo(claimNo);
    }

    // Get claims by policy ID
    @GetMapping("/api/claim/policy/v3/{policyId}")
    public List<ClaimResponseDto> getByPolicyId(@PathVariable Long policyId) {
        return claimService.getByPolicyId(policyId);
    }

    // Get all claims
    @GetMapping("/api/claim/all")
    public List<ClaimResponseDto> getAll() {
        return claimService.getAll();
    }


    // Officer triggers payout to customer's account
    @PostMapping("/api/claim/settlement/process/{claimId}")
    public void processSettlement(@PathVariable Long claimId) {
        claimService.processSettlement(claimId);
    }

    // Both Customer & Officer use this to retrieve and print the receipt
    @GetMapping("/api/claim/settlement/receipt/{claimId}")
    public ClaimSettlementReceiptDto getSettlementReceipt(@PathVariable Long claimId) {
        return claimService.getSettlementReceipt(claimId);
    }

}