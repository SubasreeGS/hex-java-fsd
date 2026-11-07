package com.springboot.AutomobileInsurance.controller;

import com.springboot.AutomobileInsurance.dto.request.OfficerDto;
import com.springboot.AutomobileInsurance.dto.request.OfficerUpdateDto;
import com.springboot.AutomobileInsurance.dto.response.*;
import com.springboot.AutomobileInsurance.enums.ClaimStatus;
import com.springboot.AutomobileInsurance.enums.PolicyType;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;
import com.springboot.AutomobileInsurance.service.OfficerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")

public class OfficerController {

    private final OfficerService officerService;

    // Add officer by admin
    @PostMapping("/api/officer/add")
    public void insertOfficer(@Valid @RequestBody OfficerDto officerDto) {

        officerService.insertOfficer(officerDto);
    }
// Existing officer endpoints (e.g. approve, reject, details)...

    @GetMapping("/api/officer/analytics")
    public OfficerAnalyticsDto getOfficerAnalytics() {
        return officerService.getOfficerAnalytics();
    }


    @GetMapping("/api/officer/proposals/filter")
    public ProposalFilterDto filterProposals(
            @RequestParam(value = "policyType", required = false) PolicyType policyType,
            @RequestParam(value = "status", required = false) ProposalStatus status,
            @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) LocalDate toDate,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size
    ) {
        return officerService.filterProposals(policyType, status, fromDate, toDate, page, size);
    }

    @GetMapping("/api/officer/claims/filter")
    public ClaimFilterDto filterClaims(
            @RequestParam(value = "status", required = false) ClaimStatus status,
            @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) LocalDate toDate,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size
    ) {
        return officerService.filterClaims(status, fromDate, toDate, page, size);
    }
    @GetMapping("/api/officer/paginated")
    public OfficerPaginationDto getOfficersPaginated(
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "5") int size) {
        return officerService.getOfficersWithPagination(page, size);
    }


/*
    @GetMapping("/api/officer/{id}")
    public OfficerResponseDto getById(@PathVariable long id) {

        return officerService.getById(id);
    }

    @GetMapping("/api/officer/all")
    public List<OfficerResponseDto> getAll() {

        return officerService.getAll();
    }

    @DeleteMapping("/api/officer/{id}")
    public void deleteById(@PathVariable long id) {

        officerService.deleteById(id);
    }

    @PutMapping("/api/officer/{id}")
    public void update(@PathVariable Long id, @RequestBody OfficerUpdateDto dto) {

        officerService.update(id, dto);
    }

 */
}