package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.OfficerDto;
import com.springboot.AutomobileInsurance.dto.response.ClaimFilterDto;
import com.springboot.AutomobileInsurance.dto.response.ClaimRespDto;
import com.springboot.AutomobileInsurance.dto.response.OfficerAnalyticsDto;
import com.springboot.AutomobileInsurance.dto.response.OfficerPaginationDto;
import com.springboot.AutomobileInsurance.dto.response.ProposalFilterDto;
import com.springboot.AutomobileInsurance.dto.response.ProposalRespDto;
import com.springboot.AutomobileInsurance.dto.response.StatDto;
import com.springboot.AutomobileInsurance.enums.ClaimStatus;
import com.springboot.AutomobileInsurance.enums.PolicyType;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;
import com.springboot.AutomobileInsurance.enums.Role;
import com.springboot.AutomobileInsurance.model.Officer;
import com.springboot.AutomobileInsurance.model.User;
import com.springboot.AutomobileInsurance.repository.ClaimRepository;
import com.springboot.AutomobileInsurance.repository.OfficerRepository;
import com.springboot.AutomobileInsurance.repository.PolicyRepository;
import com.springboot.AutomobileInsurance.repository.ProposalRepository;
import com.springboot.AutomobileInsurance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OfficerServiceTest {

    @Mock
    private OfficerRepository officerRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private OfficerService officerService;

    private OfficerDto officerDto;
    private Officer officer;
    private User officerUser;
    private ClaimRespDto claimResp;
    private ProposalRespDto proposalResp;

    @BeforeEach
    void setUp() {
        officerDto = new OfficerDto("oliver.grant", "OfficerSecure!2026", "Oliver Grant");

        officerUser = new User();
        officerUser.setId(27L);
        officerUser.setUsername("oliver.grant");
        officerUser.setPassword("encoded-password");
        officerUser.setRole(Role.OFFICER);

        officer = new Officer();
        officer.setId(38L);
        officer.setName("Oliver Grant");
        officer.setUser(officerUser);

        claimResp = new ClaimRespDto(
                64L,
                "CLM-2026-0064",
                "Anika Rao",
                new BigDecimal("38750.00"),
                ClaimStatus.FILED,
                Instant.parse("2026-05-14T10:15:00Z")
        );
        proposalResp = new ProposalRespDto(
                81L,
                "Anika Rao",
                "KA03MN4821",
                PolicyType.COMPREHENSIVE,
                ProposalStatus.PROPOSAL_SUBMITTED,
                Instant.parse("2026-05-10T08:30:00Z")
        );
    }

    @Test
    void insertOfficer_savesEncodedOfficerUserAndOfficer() {
        when(userRepository.existsByUsername("oliver.grant")).thenReturn(false);
        when(passwordEncoder.encode("OfficerSecure!2026")).thenReturn("hashed-officer-password");

        officerService.insertOfficer(officerDto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Officer> officerCaptor = ArgumentCaptor.forClass(Officer.class);
        verify(userRepository).save(userCaptor.capture());
        verify(officerRepository).save(officerCaptor.capture());
        assertEquals("oliver.grant", userCaptor.getValue().getUsername());
        assertEquals("hashed-officer-password", userCaptor.getValue().getPassword());
        assertEquals(Role.OFFICER, userCaptor.getValue().getRole());
        assertEquals("Oliver Grant", officerCaptor.getValue().getName());
        assertEquals(userCaptor.getValue(), officerCaptor.getValue().getUser());
    }

    @Test
    void insertOfficer_throwsWhenUsernameAlreadyExists() {
        when(userRepository.existsByUsername("oliver.grant")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> officerService.insertOfficer(officerDto));

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder, officerRepository);
    }

    @Test
    void insertOfficer_propagatesPasswordEncoderFailure() {
        when(userRepository.existsByUsername("oliver.grant")).thenReturn(false);
        when(passwordEncoder.encode("OfficerSecure!2026"))
                .thenThrow(new IllegalStateException("password encoder unavailable"));

        assertThrows(IllegalStateException.class, () -> officerService.insertOfficer(officerDto));

        verify(passwordEncoder).encode("OfficerSecure!2026");
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(officerRepository);
    }

    @Test
    void insertOfficer_propagatesUserRepositoryFailure() {
        when(userRepository.existsByUsername("oliver.grant")).thenReturn(false);
        when(passwordEncoder.encode("OfficerSecure!2026")).thenReturn("hashed-officer-password");
        when(userRepository.save(any(User.class))).thenThrow(new IllegalStateException("user save failed"));

        assertThrows(IllegalStateException.class, () -> officerService.insertOfficer(officerDto));

        verify(userRepository).save(any(User.class));
        verifyNoInteractions(officerRepository);
    }

    @Test
    void getOfficerAnalytics_returnsClaimAndProposalStatsAndPayout() {
        List<StatDto> claimsStats = List.of(new StatDto("SETTLED", 12L));
        List<StatDto> proposalStats = List.of(new StatDto("POLICY_ISSUED", 19L));
        when(claimRepository.getClaimsCountByStatus()).thenReturn(claimsStats);
        when(proposalRepository.getProposalsCountByStatus()).thenReturn(proposalStats);
        when(claimRepository.getTotalSettledPayout()).thenReturn(428500.75);

        OfficerAnalyticsDto response = officerService.getOfficerAnalytics();

        assertEquals(claimsStats, response.claimsStats());
        assertEquals(proposalStats, response.proposalStats());
        assertEquals(428500.75, response.totalSettledPayout());
        verify(claimRepository).getClaimsCountByStatus();
        verify(proposalRepository).getProposalsCountByStatus();
        verify(claimRepository).getTotalSettledPayout();
    }

    @Test
    void getOfficerAnalytics_propagatesRepositoryFailure() {
        when(claimRepository.getClaimsCountByStatus())
                .thenThrow(new IllegalStateException("analytics query failed"));

        assertThrows(IllegalStateException.class, () -> officerService.getOfficerAnalytics());

        verify(claimRepository).getClaimsCountByStatus();
        verifyNoInteractions(proposalRepository);
    }

    @Test
    void filterClaims_returnsFilteredClaimsAndPagination() {
        LocalDate fromDate = LocalDate.of(2026, 5, 1);
        LocalDate toDate = LocalDate.of(2026, 5, 31);
        Pageable pageable = PageRequest.of(0, 5);
        Page<ClaimRespDto> page = new PageImpl<>(List.of(claimResp), pageable, 7);
        Instant fromInstant = fromDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toInstant = toDate.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC).toInstant();
        when(claimRepository.dynamicFilterClaims(ClaimStatus.FILED, fromInstant, toInstant, pageable))
                .thenReturn(page);

        ClaimFilterDto response = officerService.filterClaims(ClaimStatus.FILED, fromDate, toDate, 0, 5);

        assertEquals(List.of(claimResp), response.list());
        assertEquals(2, response.paginationInfo().totalPages());
        assertEquals(7L, response.paginationInfo().totalRecords());
        assertEquals(1, response.paginationInfo().numRecordsInPage());
        verify(claimRepository).dynamicFilterClaims(ClaimStatus.FILED, fromInstant, toInstant, pageable);
    }

    @Test
    void filterClaims_usesDefaultsAndPropagatesRepositoryFailure() {
        Pageable defaultPageable = PageRequest.of(0, 10);
        when(claimRepository.dynamicFilterClaims(null, null, null, defaultPageable))
                .thenThrow(new IllegalStateException("claim query failed"));

        assertThrows(
                IllegalStateException.class,
                () -> officerService.filterClaims(null, null, null, -1, 0)
        );

        verify(claimRepository).dynamicFilterClaims(null, null, null, defaultPageable);
    }

    @Test
    void filterProposals_returnsFilteredProposalsAndPagination() {
        LocalDate fromDate = LocalDate.of(2026, 5, 1);
        LocalDate toDate = LocalDate.of(2026, 5, 31);
        Pageable pageable = PageRequest.of(1, 4);
        Page<ProposalRespDto> page = new PageImpl<>(List.of(proposalResp), pageable, 5);
        Instant fromInstant = fromDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toInstant = toDate.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC).toInstant();
        when(proposalRepository.dynamicFilterProposals(
                PolicyType.COMPREHENSIVE,
                ProposalStatus.PROPOSAL_SUBMITTED,
                fromInstant,
                toInstant,
                pageable
        )).thenReturn(page);

        ProposalFilterDto response = officerService.filterProposals(
                PolicyType.COMPREHENSIVE,
                ProposalStatus.PROPOSAL_SUBMITTED,
                fromDate,
                toDate,
                1,
                4
        );

        assertEquals(List.of(proposalResp), response.list());
        assertEquals(2, response.paginationInfo().totalPages());
        assertEquals(5L, response.paginationInfo().totalRecords());
        assertEquals(1, response.paginationInfo().numRecordsInPage());
        verify(proposalRepository).dynamicFilterProposals(
                PolicyType.COMPREHENSIVE,
                ProposalStatus.PROPOSAL_SUBMITTED,
                fromInstant,
                toInstant,
                pageable
        );
    }

    @Test
    void filterProposals_usesDefaultsAndPropagatesRepositoryFailure() {
        Pageable defaultPageable = PageRequest.of(0, 10);
        when(proposalRepository.dynamicFilterProposals(null, null, null, null, defaultPageable))
                .thenThrow(new IllegalStateException("proposal query failed"));

        assertThrows(
                IllegalStateException.class,
                () -> officerService.filterProposals(null, null, null, null, -4, 0)
        );

        verify(proposalRepository).dynamicFilterProposals(null, null, null, null, defaultPageable);
    }

    @Test
    void getOfficersWithPagination_returnsOfficerPageAndPagination() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<Officer> page = new PageImpl<>(List.of(officer), pageable, 4);
        when(officerRepository.findAll(pageable)).thenReturn(page);

        OfficerPaginationDto response = officerService.getOfficersWithPagination(-2, 3);

        assertEquals(1, response.list().size());
        assertEquals(38L, response.list().get(0).id());
        assertEquals("Oliver Grant", response.list().get(0).name());
        assertEquals(27L, response.list().get(0).userId());
        assertEquals("oliver.grant", response.list().get(0).username());
        assertEquals(2, response.paginationInfo().totalPages());
        assertEquals(4L, response.paginationInfo().totalRecords());
        assertEquals(1, response.paginationInfo().numRecordsInPage());
        verify(officerRepository).findAll(pageable);
    }

    @Test
    void getOfficersWithPagination_usesFallbackSizeAndPropagatesRepositoryFailure() {
        Pageable defaultPageable = PageRequest.of(0, 5);
        when(officerRepository.findAll(defaultPageable))
                .thenThrow(new IllegalStateException("officer query failed"));

        assertThrows(
                IllegalStateException.class,
                () -> officerService.getOfficersWithPagination(-1, 0)
        );

        verify(officerRepository).findAll(defaultPageable);
    }
}
