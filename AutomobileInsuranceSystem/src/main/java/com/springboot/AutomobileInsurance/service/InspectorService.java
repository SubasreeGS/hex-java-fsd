package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.InspectorDto;
import com.springboot.AutomobileInsurance.dto.request.InspectorUpdateDto;
import com.springboot.AutomobileInsurance.dto.response.InspectorResponseDto;
import com.springboot.AutomobileInsurance.enums.Role;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.mapper.InspectorMapper;
import com.springboot.AutomobileInsurance.model.Inspector;
import com.springboot.AutomobileInsurance.model.User;
import com.springboot.AutomobileInsurance.repository.InspectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InspectorService {

    private final InspectorRepository inspectorRepository;
    private final UserService userService;

    public void insertInspector(InspectorDto dto) {
        // Step 1: Prepare user obj
        User user = userService.getUserObj(
                dto.username(),
                dto.password(),
                Role.INSPECTOR
        );

        // Step 2: convert dto to inspector obj
        Inspector inspector = InspectorMapper.convertDtoToEntity(dto);

        // Step 3: inject user into inspector
        inspector.setUser(user);

        // Step 4: add inspector into db
        inspectorRepository.save(inspector);
    }

    public InspectorResponseDto getById(long id) {
        Optional<Inspector> optional = inspectorRepository.findById(id);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Invalid inspector id");
        }
        return InspectorMapper.convertEntityToDto(optional.get());
    }

    public List<InspectorResponseDto> getAll() {
        return inspectorRepository.findAll()
                .stream()
                .map(InspectorMapper::convertEntityToDto)
                .toList();
    }

    public void deleteById(long id) {
        getById(id);
        inspectorRepository.deleteById(id);
    }

    public void update(Long id, InspectorUpdateDto dto) {
        Inspector inspector = inspectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid inspector id"));

        boolean flag = false;

        if (dto.name() != null && !dto.name().trim().isEmpty()) {
            inspector.setName(dto.name());
            flag = true;
        }

        if (!flag) {
            throw new InvalidCallException("Update op terminated");
        }

        inspectorRepository.save(inspector);
    }
}