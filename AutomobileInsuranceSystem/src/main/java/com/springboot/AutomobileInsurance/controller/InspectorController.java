package com.springboot.AutomobileInsurance.controller;

import com.springboot.AutomobileInsurance.dto.request.InspectorDto;
import com.springboot.AutomobileInsurance.dto.request.InspectorUpdateDto;
import com.springboot.AutomobileInsurance.dto.response.*;
import com.springboot.AutomobileInsurance.enums.ClaimStatus;
import com.springboot.AutomobileInsurance.enums.InspectionStatus;
import com.springboot.AutomobileInsurance.enums.PolicyType;
import com.springboot.AutomobileInsurance.service.InspectorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")

public class InspectorController {

    private final InspectorService inspectorService;
    //  CREATE: Returns created InspectorResponseDto with generated ID for Redux
    @PostMapping("/api/inspector/add")

    public InspectorResponseDto insertInspector(@Valid @RequestBody InspectorDto inspectorDto, Principal principal) {
        return inspectorService.insertInspector(inspectorDto, principal.getName());
    }

    //  READ ONE: Get inspector by ID
    @GetMapping("/api/inspector/{id}")
    public InspectorResponseDto getById(@PathVariable long id) {
        return inspectorService.getById(id);
    }

    // UPDATE: Updates inspector profile (name, email, contact)
    @PutMapping("/api/inspector/{id}")
    public InspectorResponseDto update(@PathVariable Long id, @Valid @RequestBody InspectorUpdateDto dto) {
        return inspectorService.update(id, dto);
    }

    //  DELETE: Deletes inspector and linked login user
    @DeleteMapping("/api/inspector/{id}")
    public void deleteById(@PathVariable long id) {
        inspectorService.deleteById(id);
    }



    @GetMapping("/api/inspector/all")
    public List<InspectorResponseDto> getAll() {
        return inspectorService.getAll();
    }


    @GetMapping("/api/inspector/dropdown")
    public List<InspectorDropdownDto> getAllInspectors() {
        return inspectorService.getAllInspectorsForDropdown();
    }

    @GetMapping("/api/inspector/proposals/filter")
    public InspectorProposalFilterDto filterInspectorProposals(
            @RequestParam("inspectorId") Long inspectorId,
            @RequestParam(value = "policyType", required = false) PolicyType policyType,
            @RequestParam(value = "status", required = false) InspectionStatus status,
            @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) LocalDate toDate,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size
    ) {
        return inspectorService.filterInspectorProposals(
                inspectorId,
                policyType,
                status,
                fromDate,
                toDate,
                page,
                size
        );
    }

    @GetMapping("/api/inspector/claims/filter")
    public InspectorClaimFilterDto filterInspectorClaims(
            @RequestParam(value = "status", required = false) ClaimStatus status,
            @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) LocalDate toDate,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size
    ) {
        return inspectorService.filterInspectorClaims(
                status,
                fromDate,
                toDate,
                page,
                size
        );
    }

    @GetMapping("/api/inspector/metrics/{inspectorId}")
    public InspectorMetricsDto getInspectorMetrics(@PathVariable("inspectorId") Long inspectorId) {
        return inspectorService.getInspectorMetrics(inspectorId);
    }
    // In InspectorController.java
    @GetMapping("/api/inspector/profile/me")
    public InspectorResponseDto getLoggedInInspector(Principal principal) {
        return inspectorService.getByUsername(principal.getName());
    }


    @GetMapping("/api/inspector/paginated")
    public InspectorPaginationDto getInspectorsPaginated(
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "5") int size) {
        return inspectorService.getInspectorsWithPagination(page, size);
    }



}