package com.springboot.AutomobileInsurance.controller;

import com.springboot.AutomobileInsurance.dto.request.InspectorDto;
import com.springboot.AutomobileInsurance.dto.request.InspectorUpdateDto;
import com.springboot.AutomobileInsurance.dto.response.InspectorResponseDto;
import com.springboot.AutomobileInsurance.service.InspectorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor

public class InspectorController {

    private final InspectorService inspectorService;

    @PostMapping("/api/inspector/add")
    public void insertInspector(@Valid @RequestBody InspectorDto inspectorDto) {
        inspectorService.insertInspector(inspectorDto);
    }

    @GetMapping("/api/inspector/{id}")
    public InspectorResponseDto getById(@PathVariable long id) {
        return inspectorService.getById(id);
    }

    @GetMapping("/api/inspector/all")
    public List<InspectorResponseDto> getAll() {
        return inspectorService.getAll();
    }

    @DeleteMapping("/api/inspector/{id}")
    public void deleteById(@PathVariable long id) {
        inspectorService.deleteById(id);
    }

    @PutMapping("/api/inspector/{id}")
    public void update(@PathVariable Long id, @RequestBody InspectorUpdateDto dto) {
        inspectorService.update(id, dto);
    }
}