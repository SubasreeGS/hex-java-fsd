package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.InspectorDto;
import com.springboot.AutomobileInsurance.dto.request.InspectorUpdateDto;
import com.springboot.AutomobileInsurance.dto.response.InspectorDropdownDto;
import com.springboot.AutomobileInsurance.dto.response.InspectorResponseDto;
import com.springboot.AutomobileInsurance.enums.Role;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.mapper.InspectorMapper;
import com.springboot.AutomobileInsurance.model.Inspector;
import com.springboot.AutomobileInsurance.model.Officer;
import com.springboot.AutomobileInsurance.model.User;
import com.springboot.AutomobileInsurance.repository.InspectorRepository;
import com.springboot.AutomobileInsurance.repository.OfficerRepository;
import com.springboot.AutomobileInsurance.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InspectorService {

    private final InspectorRepository inspectorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OfficerRepository officerRepository;

    @Transactional
    public void insertInspector(InspectorDto dto,String officerUsername) {
        //fetch officer entity
        Officer officer = officerRepository.findByOfficerUsername(officerUsername);

        // Prepare user obj
       User user = new User();
       user.setUsername(dto.username());
       user.setPassword(passwordEncoder.encode(dto.password()));
       user.setRole(Role.INSPECTOR);
       //  save user in db
       userRepository.save(user);

        // convert dto to inspector obj
        Inspector inspector = InspectorMapper.convertDtoToEntity(dto);

        // inject user into inspector
        inspector.setUser(user);
        //set officer into inspector
        inspector.setOfficer(officer);

        //  add inspector into db
        inspectorRepository.save(inspector);
    }

    public List<InspectorDropdownDto> getAllInspectorsForDropdown() {
        return inspectorRepository.findAllForDropdown();
    }
/*
    public InspectorResponseDto getById(long id) {
       Inspector inspector = inspectorRepository.findById(id)
               .orElseThrow(()-> new ResourceNotFoundException("Invalid inspector id"));

        return InspectorMapper.convertEntityToDto(inspector);
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

 */
}