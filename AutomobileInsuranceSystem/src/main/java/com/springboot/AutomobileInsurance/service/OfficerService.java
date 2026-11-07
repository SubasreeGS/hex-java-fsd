package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.OfficerDto;
import com.springboot.AutomobileInsurance.dto.request.OfficerUpdateDto;
import com.springboot.AutomobileInsurance.dto.response.*;
import com.springboot.AutomobileInsurance.enums.*;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.mapper.OfficerMapper;
import com.springboot.AutomobileInsurance.model.Officer;
import com.springboot.AutomobileInsurance.model.User;
import com.springboot.AutomobileInsurance.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OfficerService {

    private final OfficerRepository officerRepository;
    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final ProposalRepository proposalRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void insertOfficer(OfficerDto dto) {
        //  Check if username already exists in user db
        if (userRepository.existsByUsername(dto.username().trim())) {
            throw new RuntimeException("Username '" + dto.username().trim() + "' is already taken. Please choose another.");
        }

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

    public OfficerAnalyticsDto getOfficerAnalytics() {
        List<StatDto> claimsStats = claimRepository.getClaimsCountByStatus();
        List<StatDto> proposalStats = proposalRepository.getProposalsCountByStatus();
        Double totalPayout = claimRepository.getTotalSettledPayout();

        return new OfficerAnalyticsDto(claimsStats, proposalStats, totalPayout);
    }



    public ClaimFilterDto filterClaims(ClaimStatus status, LocalDate fromDate, LocalDate toDate, Integer page, Integer size) {
        int pageNo = (page == null || page < 0) ? 0 : page;
        int pageSize = (size == null || size < 1) ? 10 : size;
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Instant fromInstant = (fromDate == null) ? null : fromDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toInstant = (toDate == null) ? null : toDate.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC).toInstant();

        Page<ClaimRespDto> pageDto = claimRepository.dynamicFilterClaims(status, fromInstant, toInstant, pageable);

        return new ClaimFilterDto(
                pageDto.getContent(),
                new PaginationDto(
                        pageDto.getTotalPages(),
                        pageDto.getTotalElements(),
                        pageDto.getNumberOfElements()
                )
        );
    }

    public ProposalFilterDto filterProposals(
            PolicyType policyType,
            ProposalStatus status,
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

        Page<ProposalRespDto> pageDto = proposalRepository.dynamicFilterProposals(
                policyType,
                status,
                fromInstant,
                toInstant,
                pageable
        );

        return new ProposalFilterDto(
                pageDto.getContent(),
                new PaginationDto(
                        pageDto.getTotalPages(),
                        pageDto.getTotalElements(),
                        pageDto.getNumberOfElements()
                )
        );
    }
    public OfficerPaginationDto getOfficersWithPagination(int page, int size) {
        int pageNo = Math.max(page, 0);
        int pageSize = (size < 1) ? 5 : size;
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Page<Officer> officerPage = officerRepository.findAll(pageable);

        List<OfficerResponseDto> dtoList = officerPage.getContent()
                .stream()
                .map(OfficerMapper::convertEntityToDto)
                .toList();

        PaginationDto paginationDto = new PaginationDto(
                officerPage.getTotalPages(),
                officerPage.getTotalElements(),
                officerPage.getNumberOfElements()
        );

        return new OfficerPaginationDto(dtoList, paginationDto);
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