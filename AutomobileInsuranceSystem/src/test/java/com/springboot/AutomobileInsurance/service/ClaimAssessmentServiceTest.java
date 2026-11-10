package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.ClaimAssessmentRequestDto;
import com.springboot.AutomobileInsurance.dto.request.SubmitAssessmentReportDto;
import com.springboot.AutomobileInsurance.dto.response.ClaimAssessmentResponseDto;
import com.springboot.AutomobileInsurance.dto.response.InspectorTaskPaginationDto;
import com.springboot.AutomobileInsurance.dto.response.InspectorTaskResponseDto;
import com.springboot.AutomobileInsurance.enums.AssessmentStatus;
import com.springboot.AutomobileInsurance.enums.ClaimStatus;
import com.springboot.AutomobileInsurance.enums.Role;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.model.Claim;
import com.springboot.AutomobileInsurance.model.ClaimAssessment;
import com.springboot.AutomobileInsurance.model.Inspector;
import com.springboot.AutomobileInsurance.model.User;
import com.springboot.AutomobileInsurance.repository.ClaimAssessmentRepository;
import com.springboot.AutomobileInsurance.repository.ClaimRepository;
import com.springboot.AutomobileInsurance.repository.InspectorRepository;
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
public class ClaimAssessmentServiceTest {

    @Mock
    private ClaimAssessmentRepository claimAssessmentRepository;

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private InspectorRepository inspectorRepository;

    @InjectMocks
    private ClaimAssessmentService claimAssessmentService;

    private Claim claim;
    private Inspector inspector;
    private ClaimAssessment assessment;
    private ClaimAssessmentResponseDto assessmentResponse;
    private InspectorTaskResponseDto taskResponse;

    @BeforeEach
    void setUp() {
        claim = new Claim();
        claim.setId(42L);
        claim.setClaimNo("CLM-2026-0042");
        claim.setIncidentDate(LocalDate.of(2026, 2, 11));
        claim.setIncidentDescription("Rear bumper damage after a parking collision");
        claim.setClaimedAmount(new BigDecimal("38750.00"));
        claim.setStatus(ClaimStatus.FILED);

        User inspectorUser = new User();
        inspectorUser.setId(18L);
        inspectorUser.setUsername("elena.morris");
        inspectorUser.setPassword("encoded-password");
        inspectorUser.setRole(Role.INSPECTOR);
        inspector = new Inspector();
        inspector.setId(29L);
        inspector.setName("Elena Morris");
        inspector.setEmail("elena.morris@example.com");
        inspector.setContact("9123456780");
        inspector.setUser(inspectorUser);

        assessment = new ClaimAssessment();
        assessment.setId(63L);
        assessment.setClaim(claim);
        assessment.setInspector(inspector);
        assessment.setEstimatedCost(new BigDecimal("25000.00"));
        assessment.setDamagePhotosUrl("https://insurance.example/claims/CLM-2026-0042/damage.jpg");
        assessment.setAssessmentNotes("Rear bumper and right tail light require repair.");
        assessment.setStatus(AssessmentStatus.PENDING);

        assessmentResponse = new ClaimAssessmentResponseDto(
                "CLM-2026-0042",
                "Elena Morris",
                new BigDecimal("25000.00"),
                "https://insurance.example/claims/CLM-2026-0042/damage.jpg",
                "Rear bumper and right tail light require repair.",
                AssessmentStatus.PENDING
        );
        taskResponse = new InspectorTaskResponseDto(
                63L,
                "CLM-2026-0042",
                "KA03MN4821",
                "Honda",
                "City",
                2022,
                "MAKAA1234AB567890",
                com.springboot.AutomobileInsurance.enums.FuelType.PETROL,
                "Anika Rao",
                "9876543210",
                "42 Jacaranda Lane, Bengaluru",
                LocalDate.of(2026, 2, 11),
                "Rear bumper damage after a parking collision",
                new BigDecimal("38750.00"),
                AssessmentStatus.PENDING
        );
    }

    @Test
    void updateAssessment_updatesProvidedFieldsAndSaves() {
        when(claimAssessmentRepository.findById(63L)).thenReturn(Optional.of(assessment));
        ClaimAssessmentRequestDto request = new ClaimAssessmentRequestDto(
                42L,
                29L,
                new BigDecimal("25100.456"),
                "https://insurance.example/claims/CLM-2026-0042/updated.jpg",
                "Updated estimate after reviewing the additional photos.",
                AssessmentStatus.IN_PROGRESS
        );

        claimAssessmentService.updateAssessment(63L, request);

        assertEquals(new BigDecimal("25100.46"), assessment.getEstimatedCost());
        assertEquals("https://insurance.example/claims/CLM-2026-0042/updated.jpg", assessment.getDamagePhotosUrl());
        assertEquals("Updated estimate after reviewing the additional photos.", assessment.getAssessmentNotes());
        assertEquals(AssessmentStatus.IN_PROGRESS, assessment.getStatus());
        verify(claimAssessmentRepository).save(assessment);
    }

    @Test
    void updateAssessment_throwsWhenAssessmentDoesNotExist() {
        when(claimAssessmentRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> claimAssessmentService.updateAssessment(
                        404L, new ClaimAssessmentRequestDto(42L, 29L, BigDecimal.TEN, null, null, null)
                )
        );

        verify(claimAssessmentRepository, never()).save(any(ClaimAssessment.class));
    }

    @Test
    void updateAssessment_throwsWhenNoFieldsAreProvided() {
        when(claimAssessmentRepository.findById(63L)).thenReturn(Optional.of(assessment));

        assertThrows(
                InvalidCallException.class,
                () -> claimAssessmentService.updateAssessment(
                        63L, new ClaimAssessmentRequestDto(42L, 29L, null, null, null, null)
                )
        );

        verify(claimAssessmentRepository, never()).save(any(ClaimAssessment.class));
    }

    @Test
    void getById_returnsAssessmentResponse() {
        when(claimAssessmentRepository.findById(63L)).thenReturn(Optional.of(assessment));

        ClaimAssessmentResponseDto response = claimAssessmentService.getById(63L);

        assertEquals(assessmentResponse, response);
        verify(claimAssessmentRepository).findById(63L);
    }

    @Test
    void getById_throwsWhenAssessmentDoesNotExist() {
        when(claimAssessmentRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> claimAssessmentService.getById(404L));

        verify(claimAssessmentRepository).findById(404L);
    }

    @Test
    void getByClaimId_returnsAssessmentResponse() {
        when(claimAssessmentRepository.findByClaimId(42L)).thenReturn(Optional.of(assessment));

        ClaimAssessmentResponseDto response = claimAssessmentService.getByClaimId(42L);

        assertEquals(assessmentResponse, response);
        verify(claimAssessmentRepository).findByClaimId(42L);
    }

    @Test
    void getByClaimId_throwsWhenAssessmentDoesNotExist() {
        when(claimAssessmentRepository.findByClaimId(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> claimAssessmentService.getByClaimId(404L));

        verify(claimAssessmentRepository).findByClaimId(404L);
    }

    @Test
    void getByInspectorId_returnsMappedAssessments() {
        when(claimAssessmentRepository.findByInspectorId(29L)).thenReturn(List.of(assessment));

        List<ClaimAssessmentResponseDto> response = claimAssessmentService.getByInspectorId(29L);

        assertEquals(List.of(assessmentResponse), response);
        verify(claimAssessmentRepository).findByInspectorId(29L);
    }

    @Test
    void getByInspectorId_propagatesRepositoryFailure() {
        when(claimAssessmentRepository.findByInspectorId(29L))
                .thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(IllegalStateException.class, () -> claimAssessmentService.getByInspectorId(29L));

        verify(claimAssessmentRepository).findByInspectorId(29L);
    }

    @Test
    void getAll_returnsMappedAssessments() {
        when(claimAssessmentRepository.findAll()).thenReturn(List.of(assessment));

        List<ClaimAssessmentResponseDto> response = claimAssessmentService.getAll();

        assertEquals(List.of(assessmentResponse), response);
        verify(claimAssessmentRepository).findAll();
    }

    @Test
    void getAll_propagatesRepositoryFailure() {
        when(claimAssessmentRepository.findAll()).thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(IllegalStateException.class, () -> claimAssessmentService.getAll());

        verify(claimAssessmentRepository).findAll();
    }

    @Test
    void getMyTasks_returnsTaskPageAndPagination() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<InspectorTaskResponseDto> taskPage = new PageImpl<>(List.of(taskResponse), pageable, 7);
        when(claimAssessmentRepository.findTasksByInspectorUsername("elena.morris", pageable))
                .thenReturn(taskPage);

        InspectorTaskPaginationDto response = claimAssessmentService.getMyTasks("elena.morris", 0, 5);

        assertEquals(List.of(taskResponse), response.list());
        assertEquals(2, response.paginationInfo().totalPages());
        assertEquals(7L, response.paginationInfo().totalRecords());
        assertEquals(1, response.paginationInfo().numRecordsInPage());
        verify(claimAssessmentRepository).findTasksByInspectorUsername("elena.morris", pageable);
    }

    @Test
    void getMyTasks_propagatesRepositoryFailure() {
        when(claimAssessmentRepository.findTasksByInspectorUsername(eq("elena.morris"), any(Pageable.class)))
                .thenThrow(new IllegalStateException("query failed"));

        assertThrows(
                IllegalStateException.class,
                () -> claimAssessmentService.getMyTasks("elena.morris", 0, 5)
        );

        verify(claimAssessmentRepository).findTasksByInspectorUsername("elena.morris", PageRequest.of(0, 5));
    }

    @Test
    void getMyTasks_throwsForInvalidPageableArguments() {
        assertThrows(
                IllegalArgumentException.class,
                () -> claimAssessmentService.getMyTasks("elena.morris", -1, 5)
        );

        verifyNoInteractions(claimAssessmentRepository);
    }

    @Test
    void submitSurveyReport_updatesAssessmentAndClaim() {
        when(claimAssessmentRepository.findById(63L)).thenReturn(Optional.of(assessment));
        SubmitAssessmentReportDto report = new SubmitAssessmentReportDto(
                new BigDecimal("26780.456"),
                "https://insurance.example/claims/CLM-2026-0042/final-damage.jpg",
                "Assessment complete; repair cost includes the tail light assembly."
        );

        claimAssessmentService.submitSurveyReport(63L, report, "elena.morris");

        assertEquals(new BigDecimal("26780.46"), assessment.getEstimatedCost());
        assertEquals(report.damagePhotosUrl(), assessment.getDamagePhotosUrl());
        assertEquals(report.assessmentNotes(), assessment.getAssessmentNotes());
        assertEquals(AssessmentStatus.COMPLETED, assessment.getStatus());
        assertEquals(ClaimStatus.UNDER_ASSESSMENT, claim.getStatus());
        verify(claimAssessmentRepository).save(assessment);
        verify(claimRepository).save(claim);
    }

    @Test
    void submitSurveyReport_throwsWhenAssessmentDoesNotExist() {
        when(claimAssessmentRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> claimAssessmentService.submitSurveyReport(
                        404L,
                        new SubmitAssessmentReportDto(BigDecimal.TEN, "https://example.com/damage.jpg", "Repair needed."),
                        "elena.morris"
                )
        );

        verify(claimAssessmentRepository, never()).save(any(ClaimAssessment.class));
        verifyNoInteractions(claimRepository);
    }

    @Test
    void submitSurveyReport_throwsWhenInspectorIsNotAssigned() {
        when(claimAssessmentRepository.findById(63L)).thenReturn(Optional.of(assessment));

        assertThrows(
                InvalidCallException.class,
                () -> claimAssessmentService.submitSurveyReport(
                        63L,
                        new SubmitAssessmentReportDto(BigDecimal.TEN, "https://example.com/damage.jpg", "Repair needed."),
                        "unauthorized.inspector"
                )
        );

        verify(claimAssessmentRepository, never()).save(any(ClaimAssessment.class));
        verifyNoInteractions(claimRepository);
    }
}
