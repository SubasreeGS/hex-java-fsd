package com.springboot.AutomobileInsurance.controller;

import com.springboot.AutomobileInsurance.dto.request.AdminDto;
import com.springboot.AutomobileInsurance.dto.response.AdminStatsDto;
import com.springboot.AutomobileInsurance.repository.*;
import com.springboot.AutomobileInsurance.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")

public class AdminController {
   private final UserService userService;
   private final OfficerRepository officerRepository;
    private final InspectorRepository inspectorRepository;
    private final CustomerRepository customerRepository;
    private final PolicyRepository policyRepository;

    //create admin
    @PostMapping("/api/admin/add")
    public void insertAdmin(@RequestBody AdminDto dto)
    {
        userService.insertAdmin(dto);
    }

    @GetMapping("/api/admin/stats")
    public AdminStatsDto getAdminStats() {
        long officersCount = officerRepository.count();
        long inspectorsCount = inspectorRepository.count();
        long customersCount = customerRepository.count();
        long policiesCount = policyRepository.count();

        return new AdminStatsDto(officersCount, inspectorsCount, customersCount, policiesCount);
    }
}
