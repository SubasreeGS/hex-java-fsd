package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.InspectorDto;
import com.springboot.AutomobileInsurance.dto.request.InspectorUpdateDto;
import com.springboot.AutomobileInsurance.dto.response.*;
import com.springboot.AutomobileInsurance.enums.ClaimStatus;
import com.springboot.AutomobileInsurance.enums.InspectionStatus;
import com.springboot.AutomobileInsurance.enums.PolicyType;
import com.springboot.AutomobileInsurance.enums.Role;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.mapper.InspectorMapper;
import com.springboot.AutomobileInsurance.model.Inspector;
import com.springboot.AutomobileInsurance.model.Officer;
import com.springboot.AutomobileInsurance.model.User;
import com.springboot.AutomobileInsurance.repository.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InspectorService {

    private final InspectorRepository inspectorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OfficerRepository officerRepository;
    private final InspectionRepository inspectionRepository;
    private final ClaimRepository claimRepository;
    private final Logger log = LoggerFactory.getLogger(InspectorService.class);

    @Transactional
    public InspectorResponseDto insertInspector(InspectorDto dto,String officerUsername) {
        log.info("Officer '{}' initiating onboarding for Inspector: '{}'", officerUsername, dto.username());
        //  Check if username already exists in user db
        if (userRepository.existsByUsername(dto.username().trim())) {
            log.warn("Inspector creation rejected: Username '{}' is duplicate", dto.username());
            throw new RuntimeException("Username '" + dto.username().trim() + "' is already taken. Please choose another.");
        }

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
        Inspector savedInspector = inspectorRepository.save(inspector);
        log.info("Inspector '{}' registered successfully with ID: {} linked to Officer: '{}'",
                savedInspector.getName(), savedInspector.getId(), officerUsername);

        return InspectorMapper.convertEntityToDto(savedInspector);

    }

    public List<InspectorDropdownDto> getAllInspectorsForDropdown() {
        return inspectorRepository.findAllForDropdown();
    }


    public InspectorResponseDto getById(long id) {

        Inspector inspector = inspectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inspector not found"));
        return InspectorMapper.convertEntityToDto(inspector);
    }

    public InspectorResponseDto update(Long id, @Valid InspectorUpdateDto dto) {
        Inspector inspector = inspectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid inspector id"));

        boolean updated = false;

        if (dto.name() != null && !dto.name().trim().isEmpty()) {
            inspector.setName(dto.name().trim());
            updated = true;
        }

        if (dto.email() != null && !dto.email().trim().isEmpty()) {
            inspector.setEmail(dto.email().trim());
            updated = true;
        }

        if (dto.contact() != null && !dto.contact().trim().isEmpty()) {
            inspector.setContact(dto.contact().trim());
            updated = true;
        }

        if (!updated) {
            throw new InvalidCallException("No valid fields provided for update.");
        }

        Inspector savedInspector = inspectorRepository.save(inspector);
        return InspectorMapper.convertEntityToDto(savedInspector);
    }

    public void deleteById(long id) {
        log.warn("Initiating deletion of Inspector ID: {}", id);
        Inspector inspector = inspectorRepository.findById(id)

                .orElseThrow(() -> new ResourceNotFoundException("Inspector record not found with id: " + id));

        Long userId = (inspector.getUser() != null) ? inspector.getUser().getId() : null;

        // Delete inspector profile first
        inspectorRepository.delete(inspector);
        log.info("Inspector ID: {} and associated credentials deleted successfully", id);

        // Delete linked auth user
        if (userId != null) {
            userRepository.deleteById(userId);
        }
    }




    public List<InspectorResponseDto> getAll() {
        return inspectorRepository.findAll()
                .stream()
                .map(InspectorMapper::convertEntityToDto)
                .toList();
    }


    public InspectorProposalFilterDto filterInspectorProposals(
            Long inspectorId,
            PolicyType policyType,
            InspectionStatus status,
            LocalDate fromDate,
            LocalDate toDate,
            Integer page,
            Integer size
    ) {
        int pageNo = (page == null || page < 0) ? 0 : page;
        int pageSize = (size == null || size < 1) ? 10 : size;
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Instant fromInstant = (fromDate == null) ? null : fromDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toInstant = (toDate == null) ? null : toDate.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC).toInstant();

        Page<InspectorProposalRespDto> pageDto = inspectionRepository.dynamicFilterInspectorProposals(
                inspectorId,
                policyType,
                status,
                fromInstant,
                toInstant,
                pageable
        );

        return new InspectorProposalFilterDto(
                pageDto.getContent(),
                new PaginationDto(
                        pageDto.getTotalPages(),
                        pageDto.getTotalElements(),
                        pageDto.getNumberOfElements()
                )
        );
    }

    public InspectorClaimFilterDto filterInspectorClaims(
            ClaimStatus status,
            LocalDate fromDate,
            LocalDate toDate,
            Integer page,
            Integer size
    ) {
        int pageNo = (page == null || page < 0) ? 0 : page;
        int pageSize = (size == null || size < 1) ? 10 : size;
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Page<InspectorClaimRespDto> pageDto = claimRepository.dynamicFilterInspectorClaims(
                status,
                fromDate,
                toDate,
                pageable
        );

        return new InspectorClaimFilterDto(
                pageDto.getContent(),
                new PaginationDto(
                        pageDto.getTotalPages(),
                        pageDto.getTotalElements(),
                        pageDto.getNumberOfElements()
                )
        );
    }

    public InspectorMetricsDto getInspectorMetrics(Long inspectorId) {
        long pendingVehicleSurveys = inspectionRepository.countPendingSurveysByInspector(inspectorId);
        long completedSurveys = inspectionRepository.countCompletedSurveysByInspector(inspectorId);
        long pendingClaimSurveys = claimRepository.countPendingClaimAssessments();

        return new InspectorMetricsDto(pendingVehicleSurveys, pendingClaimSurveys, completedSurveys);
    }


    // In InspectorService.java
    public InspectorResponseDto getByUsername(String username) {
        Inspector inspector = inspectorRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Inspector not found for logged-in user: " + username));
        return InspectorMapper.convertEntityToDto(inspector);
    }


    public InspectorPaginationDto getInspectorsWithPagination(int page, int size) {
        int pageNo = Math.max(page, 0);
        int pageSize = (size < 1) ? 5 : size;
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Page<Inspector> inspectorPage = inspectorRepository.findAll(pageable);

        List<InspectorResponseDto> dtoList = inspectorPage.getContent()
                .stream()
                .map(InspectorMapper::convertEntityToDto)
                .toList();

        PaginationDto paginationDto = new PaginationDto(
                inspectorPage.getTotalPages(),
                inspectorPage.getTotalElements(),
                inspectorPage.getNumberOfElements()
        );

        return new InspectorPaginationDto(dtoList, paginationDto);
    }

    public List<InspectorDropdownDto> getMyInspectorsForDropdown(String officerUsername) {
        return inspectorRepository.findMyInspectorsForDropdown(officerUsername);
    }
}