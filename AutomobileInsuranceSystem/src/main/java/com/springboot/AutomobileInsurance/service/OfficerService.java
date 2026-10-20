package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.OfficerDto;
import com.springboot.AutomobileInsurance.dto.request.OfficerUpdateDto;
import com.springboot.AutomobileInsurance.dto.response.OfficerResponseDto;
import com.springboot.AutomobileInsurance.enums.Role;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.mapper.OfficerMapper;
import com.springboot.AutomobileInsurance.model.Officer;
import com.springboot.AutomobileInsurance.model.User;
import com.springboot.AutomobileInsurance.repository.OfficerRepository;
import com.springboot.AutomobileInsurance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OfficerService {

    private final OfficerRepository officerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void insertOfficer(OfficerDto dto) {
        // Step 1: Prepare user
       User user = new User();
       user.setUsername(dto.username());
       user.setPassword(passwordEncoder.encode(dto.password()));
       user.setRole(Role.OFFICER);
        // Step 1.5: save user in db
       userRepository.save(user);

        // Step 2: Map dto to obj
        Officer officer = OfficerMapper.convertDtoToEntity(dto);

        // Step 3: Attach User to Officer
        officer.setUser(user);

        // Step 4: Save officer
        officerRepository.save(officer);
    }
/*
    public OfficerResponseDto getById(long id) {
        Officer officer = officerRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Invalid officer id"));

        return OfficerMapper.convertEntityToDto(officer);
    }

    public List<OfficerResponseDto> getAll() {
        return officerRepository.findAll()
                .stream()
                .map(OfficerMapper::convertEntityToDto)
                .toList();
    }

    public void deleteById(long id) {
        getById(id);
        officerRepository.deleteById(id);
    }

    public void update(Long id, OfficerUpdateDto dto) {
        Officer officer = officerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid officer id"));

        boolean flag = false;

        if (dto.name() != null && !dto.name().trim().isEmpty()) {
            officer.setName(dto.name());
            flag = true;
        }

        if (!flag) {
            throw new InvalidCallException("Update op terminated");
        }

        officerRepository.save(officer);
    }

 */
}