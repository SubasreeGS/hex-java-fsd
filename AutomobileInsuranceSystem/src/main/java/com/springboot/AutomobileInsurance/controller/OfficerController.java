package com.springboot.AutomobileInsurance.controller;

import com.springboot.AutomobileInsurance.dto.request.OfficerDto;
import com.springboot.AutomobileInsurance.dto.request.OfficerUpdateDto;
import com.springboot.AutomobileInsurance.dto.response.OfficerResponseDto;
import com.springboot.AutomobileInsurance.service.OfficerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor

public class OfficerController {

    private final OfficerService officerService;

    @PostMapping("/api/officer/add")
    public void insertOfficer(@Valid @RequestBody OfficerDto officerDto) {

        officerService.insertOfficer(officerDto);
    }

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
}