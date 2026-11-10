package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.ClaimFileRequestDto;
import com.springboot.AutomobileInsurance.dto.request.ClaimReviewRequestDto;
import com.springboot.AutomobileInsurance.dto.response.ClaimResponseDto;
import com.springboot.AutomobileInsurance.dto.response.ClaimResponseDtoV2;
import com.springboot.AutomobileInsurance.dto.response.ClaimResponseV2PaginationDto;
import com.springboot.AutomobileInsurance.dto.response.ClaimSettlementReceiptDto;
import com.springboot.AutomobileInsurance.enums.AssessmentStatus;
import com.springboot.AutomobileInsurance.enums.ClaimStatus;
import com.springboot.AutomobileInsurance.enums.PolicyStatus;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.model.Claim;
import com.springboot.AutomobileInsurance.model.ClaimAssessment;
import com.springboot.AutomobileInsurance.model.Inspector;
import com.springboot.AutomobileInsurance.model.Officer;
import com.springboot.AutomobileInsurance.model.Payment;
import com.springboot.AutomobileInsurance.model.Policy;
import com.springboot.AutomobileInsurance.model.User;
import com.springboot.AutomobileInsurance.repository.ClaimAssessmentRepository;
import com.springboot.AutomobileInsurance.repository.ClaimRepository;
import com.springboot.AutomobileInsurance.repository.InspectorRepository;
import com.springboot.AutomobileInsurance.repository.OfficerRepository;
import com.springboot.AutomobileInsurance.repository.PolicyRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private OfficerRepository officerRepository;

    @Mock
    private InspectorRepository inspectorRepository;

    @Mock
    private ClaimAssessmentRepository claimAssessmentRepository;

    @InjectMocks
    private ClaimService claimService;

    private Policy policy;
    private Claim claim;
    private Officer officer;
    private Inspector inspector;
    private ClaimResponseDto claimResponse;
    private ClaimResponseDtoV2 officerClaimResponse;

    @BeforeEach
    void setUp() {
        policy = new Policy();
        policy.setId(15L);
        policy.setPolicyNumber("POL-2026-0015");
        policy.setStartDate(LocalDate.of(2026, 1, 1));
        policy.setEndDate(LocalDate.of(2026, 12, 31));
        policy.setStatus(PolicyStatus.ACTIVE);

        User officerUser = new User();
        officerUser.setId(23L);
        officerUser.setUsername("oliver.grant");
        officerUser.setPassword("encoded-password");
        officer = new Officer();
        officer.setId(31L);
        officer.setName("Oliver Grant");
        officer.setUser(officerUser);

        User inspectorUser = new User();
        inspectorUser.setId(42L);
        inspectorUser.setUsername("elena.morris");
        inspectorUser.setPassword("encoded-inspector-password");
        inspector = new Inspector();
        inspector.setId(53L);
        inspector.setName("Elena Morris");
        inspector.setEmail("elena.morris@example.com");
        inspector.setContact("9123456780");
        inspector.setUser(inspectorUser);
        inspector.setOfficer(officer);

        claim = new Claim();
        claim.setId(64L);
        claim.setClaimNo("CLM-2026-0064");
        claim.setPolicy(policy);
        claim.setIncidentDate(LocalDate.of(2026, 5, 14));
        claim.setIncidentDescription("Side door dent following a parking-lot collision");
        claim.setClaimedAmount(new BigDecimal("38750.00"));
        claim.setApprovedAmount(BigDecimal.ZERO.setScale(2));
        claim.setStatus(ClaimStatus.FILED);
        claim.setOfficer(officer);

        claimResponse = new ClaimResponseDto(
                64L,
                "CLM-2026-0064",
                "POL-2026-0015",
                LocalDate.of(2026, 5, 14),
                "Side door dent following a parking-lot collision",
                new BigDecimal("38750.00"),
                BigDecimal.ZERO.setScale(2),
                ClaimStatus.FILED,
                "Oliver Grant",
                null,
                null
        );
        officerClaimResponse = new ClaimResponseDtoV2(
                64L,
                "CLM-2026-0064",
                "POL-2026-0015",
                LocalDate.of(2026, 5, 14),
                "Side door dent following a parking-lot collision",
                new BigDecimal("38750.00"),
                BigDecimal.ZERO.setScale(2),
                ClaimStatus.FILED,
                "Oliver Grant",
                "Elena Morris",
                null,
                null
        );
    }

    @Test
    void fileClaim_savesClaimWithUniqueNumberAndRoundedAmounts() {
        when(policyRepository.findById(15L)).thenReturn(Optional.of(policy));
        when(claimRepository.existsByClaimNo(any(String.class))).thenReturn(false);

        claimService.fileClaim(new ClaimFileRequestDto(
                15L,
                LocalDate.of(2026, 5, 14),
                "Side door dent following a parking-lot collision",
                new BigDecimal("38750.456")
        ));

        ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
        verify(claimRepository).save(claimCaptor.capture());
        Claim savedClaim = claimCaptor.getValue();
        assertEquals(policy, savedClaim.getPolicy());
        assertEquals(LocalDate.of(2026, 5, 14), savedClaim.getIncidentDate());
        assertEquals(new BigDecimal("38750.46"), savedClaim.getClaimedAmount());
        assertEquals(new BigDecimal("0.00"), savedClaim.getApprovedAmount());
        assertEquals(ClaimStatus.FILED, savedClaim.getStatus());
        org.junit.jupiter.api.Assertions.assertTrue(savedClaim.getClaimNo().matches("CLM-\\d{4}-\\d+"));
        verify(claimRepository).existsByClaimNo(any(String.class));
    }

    @Test
    void fileClaim_retriesWhenGeneratedNumberAlreadyExists() {
        when(policyRepository.findById(15L)).thenReturn(Optional.of(policy));
        when(claimRepository.existsByClaimNo(any(String.class))).thenReturn(true, false);

        claimService.fileClaim(new ClaimFileRequestDto(
                15L, LocalDate.of(2026, 5, 14), "Parking-lot collision damage", new BigDecimal("1200.00")
        ));

        verify(claimRepository, org.mockito.Mockito.times(2)).existsByClaimNo(any(String.class));
        verify(claimRepository).save(any(Claim.class));
    }

    @Test
    void fileClaim_throwsWhenPolicyDoesNotExist() {
        when(policyRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> claimService.fileClaim(
                        new ClaimFileRequestDto(404L, LocalDate.of(2026, 5, 14), "Collision damage", BigDecimal.TEN)
                )
        );

        verify(claimRepository, never()).save(any(Claim.class));
        verifyNoInteractions(claimRepository);
    }

    @Test
    void fileClaim_throwsWhenIncidentDateIsOutsideCoverage() {
        when(policyRepository.findById(15L)).thenReturn(Optional.of(policy));

        assertThrows(
                InvalidCallException.class,
                () -> claimService.fileClaim(
                        new ClaimFileRequestDto(15L, LocalDate.of(2027, 1, 1), "Collision damage", BigDecimal.TEN)
                )
        );

        verify(claimRepository, never()).save(any(Claim.class));
        verify(claimRepository, never()).existsByClaimNo(any(String.class));
    }

    @Test
    void getById_returnsClaimResponse() {
        when(claimRepository.findById(64L)).thenReturn(Optional.of(claim));

        ClaimResponseDto response = claimService.getById(64L);

        assertEquals(claimResponse, response);
        verify(claimRepository).findById(64L);
    }

    @Test
    void getById_throwsWhenClaimDoesNotExist() {
        when(claimRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> claimService.getById(404L));

        verify(claimRepository).findById(404L);
    }

    @Test
    void getByClaimNo_returnsClaimResponse() {
        when(claimRepository.findByClaimNo("CLM-2026-0064")).thenReturn(Optional.of(claim));

        ClaimResponseDto response = claimService.getByClaimNo("CLM-2026-0064");

        assertEquals(claimResponse, response);
        verify(claimRepository).findByClaimNo("CLM-2026-0064");
    }

    @Test
    void getByClaimNo_throwsWhenClaimDoesNotExist() {
        when(claimRepository.findByClaimNo("CLM-404")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> claimService.getByClaimNo("CLM-404"));

        verify(claimRepository).findByClaimNo("CLM-404");
    }

    @Test
    void getByPolicyId_returnsMappedClaims() {
        when(claimRepository.findByPolicyId(15L)).thenReturn(List.of(claim));

        List<ClaimResponseDto> response = claimService.getByPolicyId(15L);

        assertEquals(List.of(claimResponse), response);
        verify(claimRepository).findByPolicyId(15L);
    }

    @Test
    void getByPolicyId_propagatesRepositoryFailure() {
        when(claimRepository.findByPolicyId(15L)).thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(IllegalStateException.class, () -> claimService.getByPolicyId(15L));

        verify(claimRepository).findByPolicyId(15L);
    }

    @Test
    void getAll_returnsMappedClaims() {
        when(claimRepository.findAll()).thenReturn(List.of(claim));

        List<ClaimResponseDto> response = claimService.getAll();

        assertEquals(List.of(claimResponse), response);
        verify(claimRepository).findAll();
    }

    @Test
    void getAll_propagatesRepositoryFailure() {
        when(claimRepository.findAll()).thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(IllegalStateException.class, () -> claimService.getAll());

        verify(claimRepository).findAll();
    }

    @Test
    void getClaimsByCustomerUsername_returnsCustomerClaims() {
        when(claimRepository.findClaimsByCustomerUsername("anika.rao")).thenReturn(List.of(claimResponse));

        List<ClaimResponseDto> response = claimService.getClaimsByCustomerUsername("anika.rao");

        assertEquals(List.of(claimResponse), response);
        verify(claimRepository).findClaimsByCustomerUsername("anika.rao");
    }

    @Test
    void getClaimsByCustomerUsername_propagatesRepositoryFailure() {
        when(claimRepository.findClaimsByCustomerUsername("anika.rao"))
                .thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(
                IllegalStateException.class,
                () -> claimService.getClaimsByCustomerUsername("anika.rao")
        );

        verify(claimRepository).findClaimsByCustomerUsername("anika.rao");
    }

    @Test
    void assignInspector_createsPendingAssessmentAndUpdatesClaim() {
        when(claimRepository.findById(64L)).thenReturn(Optional.of(claim));
        when(inspectorRepository.findById(53L)).thenReturn(Optional.of(inspector));
        when(officerRepository.findByOfficerUsername("oliver.grant")).thenReturn(officer);

        claimService.assignInspector(53L, 64L, "oliver.grant");

        ArgumentCaptor<ClaimAssessment> assessmentCaptor = ArgumentCaptor.forClass(ClaimAssessment.class);
        verify(claimAssessmentRepository).save(assessmentCaptor.capture());
        ClaimAssessment savedAssessment = assessmentCaptor.getValue();
        assertEquals(claim, savedAssessment.getClaim());
        assertEquals(inspector, savedAssessment.getInspector());
        assertEquals(AssessmentStatus.PENDING, savedAssessment.getStatus());
        assertEquals(BigDecimal.ZERO, savedAssessment.getEstimatedCost());
        assertEquals(officer, claim.getOfficer());
        assertEquals(ClaimStatus.UNDER_ASSESSMENT, claim.getStatus());
        verify(claimRepository).save(claim);
    }

    @Test
    void assignInspector_throwsWhenClaimDoesNotExist() {
        when(claimRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> claimService.assignInspector(53L, 404L, "oliver.grant")
        );

        verifyNoInteractions(inspectorRepository, officerRepository, claimAssessmentRepository);
        verify(claimRepository, never()).save(any(Claim.class));
    }

    @Test
    void assignInspector_throwsWhenInspectorDoesNotExist() {
        when(claimRepository.findById(64L)).thenReturn(Optional.of(claim));
        when(inspectorRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> claimService.assignInspector(404L, 64L, "oliver.grant")
        );

        verifyNoInteractions(officerRepository, claimAssessmentRepository);
        verify(claimRepository, never()).save(any(Claim.class));
    }

    @Test
    void getAllClaimsForOfficer_returnsPaginatedClaims() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<ClaimResponseDtoV2> page = new PageImpl<>(List.of(officerClaimResponse), pageable, 7);
        when(claimRepository.findAllClaimsForOfficer(pageable)).thenReturn(page);

        ClaimResponseV2PaginationDto response = claimService.getAllClaimsForOfficer(0, 5);

        assertEquals(List.of(officerClaimResponse), response.list());
        assertEquals(2, response.paginationInfo().totalPages());
        assertEquals(7L, response.paginationInfo().totalRecords());
        assertEquals(1, response.paginationInfo().numRecordsInPage());
        verify(claimRepository).findAllClaimsForOfficer(pageable);
    }

    @Test
    void getAllClaimsForOfficer_propagatesRepositoryFailure() {
        when(claimRepository.findAllClaimsForOfficer(any(Pageable.class)))
                .thenThrow(new IllegalStateException("query failed"));

        assertThrows(IllegalStateException.class, () -> claimService.getAllClaimsForOfficer(0, 5));

        verify(claimRepository).findAllClaimsForOfficer(PageRequest.of(0, 5));
    }

    @Test
    void getAllClaimsForOfficer_throwsForInvalidPageableArguments() {
        assertThrows(IllegalArgumentException.class, () -> claimService.getAllClaimsForOfficer(-1, 5));

        verifyNoInteractions(claimRepository);
    }

    @Test
    void reviewClaim_approvesClaimAndRoundsApprovedAmount() {
        when(claimRepository.findById(64L)).thenReturn(Optional.of(claim));

        claimService.reviewClaim(
                64L,
                new ClaimReviewRequestDto(ClaimStatus.APPROVED, new BigDecimal("31500.456"))
        );

        assertEquals(new BigDecimal("31500.46"), claim.getApprovedAmount());
        assertEquals(ClaimStatus.APPROVED, claim.getStatus());
        verify(claimRepository).save(claim);
    }

    @Test
    void reviewClaim_rejectsClaimAndSetsZeroApprovedAmount() {
        when(claimRepository.findById(64L)).thenReturn(Optional.of(claim));

        claimService.reviewClaim(64L, new ClaimReviewRequestDto(ClaimStatus.REJECTED, null));

        assertEquals(BigDecimal.ZERO, claim.getApprovedAmount());
        assertEquals(ClaimStatus.REJECTED, claim.getStatus());
        verify(claimRepository).save(claim);
    }

    @Test
    void reviewClaim_throwsWhenClaimDoesNotExist() {
        when(claimRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> claimService.reviewClaim(
                        404L, new ClaimReviewRequestDto(ClaimStatus.APPROVED, new BigDecimal("1000.00"))
                )
        );

        verify(claimRepository, never()).save(any(Claim.class));
    }

    @Test
    void processSettlement_setsSettledStatusAndMetadata() {
        Payment payment = new Payment();
        payment.setTransactionRef("TXN-2026-1001");
        policy.setPayment(payment);
        when(claimRepository.findById(64L)).thenReturn(Optional.of(claim));

        claimService.processSettlement(64L);

        assertEquals(ClaimStatus.SETTLED, claim.getStatus());
        org.junit.jupiter.api.Assertions.assertNotNull(claim.getSettlementRef());
        org.junit.jupiter.api.Assertions.assertTrue(claim.getSettlementRef().startsWith("SETTLE-"));
        org.junit.jupiter.api.Assertions.assertTrue(claim.getSettlementRef().endsWith("-TXN-2026-1001"));
        org.junit.jupiter.api.Assertions.assertNotNull(claim.getSettledDate());
        verify(claimRepository).save(claim);
    }

    @Test
    void processSettlement_throwsWhenClaimDoesNotExist() {
        when(claimRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> claimService.processSettlement(404L));

        verify(claimRepository, never()).save(any(Claim.class));
    }

    @Test
    void getSettlementReceipt_returnsReceiptForSettledClaim() {
        claim.setStatus(ClaimStatus.SETTLED);
        ClaimSettlementReceiptDto receipt = new ClaimSettlementReceiptDto(
                64L,
                "CLM-2026-0064",
                "POL-2026-0015",
                "Anika Rao",
                "9876543210",
                "KA03MN4821",
                "TXN-2026-1001",
                new BigDecimal("38750.00"),
                new BigDecimal("31500.00"),
                "SETTLE-123456-TXN-2026-1001",
                java.time.LocalDateTime.of(2026, 5, 20, 10, 30),
                ClaimStatus.SETTLED,
                "oliver.grant"
        );
        when(claimRepository.findById(64L)).thenReturn(Optional.of(claim));
        when(claimRepository.getSettlementReceiptByClaimId(64L)).thenReturn(receipt);

        ClaimSettlementReceiptDto response = claimService.getSettlementReceipt(64L);

        assertEquals(receipt, response);
        verify(claimRepository).findById(64L);
        verify(claimRepository).getSettlementReceiptByClaimId(64L);
    }

    @Test
    void getSettlementReceipt_throwsWhenClaimIsNotSettled() {
        when(claimRepository.findById(64L)).thenReturn(Optional.of(claim));

        assertThrows(InvalidCallException.class, () -> claimService.getSettlementReceipt(64L));

        verify(claimRepository, never()).getSettlementReceiptByClaimId(64L);
    }

    @Test
    void getSettlementReceipt_throwsWhenClaimDoesNotExist() {
        when(claimRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> claimService.getSettlementReceipt(404L));

        verify(claimRepository, never()).getSettlementReceiptByClaimId(404L);
    }
}
