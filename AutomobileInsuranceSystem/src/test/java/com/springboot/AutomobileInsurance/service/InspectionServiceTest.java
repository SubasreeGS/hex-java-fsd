package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.SubmitInspectionReportDto;
import com.springboot.AutomobileInsurance.dto.response.InspectionResponseDto;
import com.springboot.AutomobileInsurance.dto.response.InspectionResponseDtoV2;
import com.springboot.AutomobileInsurance.enums.InspectionResult;
import com.springboot.AutomobileInsurance.enums.InspectionStatus;
import com.springboot.AutomobileInsurance.enums.PolicyType;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.model.Inspection;
import com.springboot.AutomobileInsurance.model.Inspector;
import com.springboot.AutomobileInsurance.model.Proposal;
import com.springboot.AutomobileInsurance.repository.InspectionRepository;
import com.springboot.AutomobileInsurance.repository.ProposalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
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
public class InspectionServiceTest {

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private ProposalRepository proposalRepository;

    @InjectMocks
    private InspectionService inspectionService;

    private Proposal proposal;
    private Inspector inspector;
    private Inspection inspection;
    private InspectionResponseDto reportResponse;
    private InspectionResponseDtoV2 detailedResponse;

    @BeforeEach
    void setUp() {
        proposal = new Proposal();
        proposal.setId(42L);
        proposal.setPolicyType(PolicyType.COMPREHENSIVE);
        proposal.setStatus(ProposalStatus.INSPECTOR_ASSIGNED);

        inspector = new Inspector();
        inspector.setId(29L);
        inspector.setName("Elena Morris");
        inspector.setEmail("elena.morris@example.com");
        inspector.setContact("9123456780");

        inspection = new Inspection();
        inspection.setId(63L);
        inspection.setProposal(proposal);
        inspection.setInspector(inspector);
        inspection.setResult(InspectionResult.PASSED);
        inspection.setRecommendedIdv(new BigDecimal("825000.00"));
        inspection.setApprovedIdv(new BigDecimal("800000.00"));
        inspection.setInspectionPhotosUrl("https://insurance.example/inspections/42/vehicle.jpg");
        inspection.setInspectionNotes("Vehicle condition verified; minor wear noted.");
        inspection.setStatus(InspectionStatus.IN_PROGRESS);

        reportResponse = new InspectionResponseDto(
                42L,
                InspectionResult.PASSED,
                new BigDecimal("825000.00"),
                "https://insurance.example/inspections/42/vehicle.jpg",
                "Vehicle condition verified; minor wear noted.",
                InspectionStatus.IN_PROGRESS
        );
        detailedResponse = new InspectionResponseDtoV2(
                63L,
                42L,
                ProposalStatus.INSPECTOR_ASSIGNED,
                29L,
                "Elena Morris",
                InspectionResult.PASSED,
                new BigDecimal("825000.00"),
                new BigDecimal("800000.00"),
                "https://insurance.example/inspections/42/vehicle.jpg",
                "Vehicle condition verified; minor wear noted.",
                InspectionStatus.IN_PROGRESS
        );
    }

    @Test
    void submitReport_savesPassedInspectionAndCompletesProposalInspection() {
        when(inspectionRepository.findByProposalId(42L)).thenReturn(Optional.of(inspection));
        SubmitInspectionReportDto report = new SubmitInspectionReportDto(
                InspectionResult.PASSED,
                new BigDecimal("840000.00"),
                "https://insurance.example/inspections/42/final.jpg",
                "Vehicle passed inspection; estimated value recorded."
        );

        inspectionService.submitReport(42L, report);

        assertEquals(ProposalStatus.INSPECTION_COMPLETED, proposal.getStatus());
        assertEquals(new BigDecimal("840000.00"), inspection.getRecommendedIdv());
        assertEquals(InspectionResult.PASSED, inspection.getResult());
        assertEquals(report.inspectionPhotosUrl(), inspection.getInspectionPhotosUrl());
        assertEquals(report.inspectionNotes(), inspection.getInspectionNotes());
        assertEquals(InspectionStatus.COMPLETED, inspection.getStatus());
        verify(inspectionRepository).save(inspection);
        verify(proposalRepository).save(proposal);
    }

    @Test
    void submitReport_savesFailedInspectionAndRejectsProposal() {
        when(inspectionRepository.findByProposalId(42L)).thenReturn(Optional.of(inspection));
        SubmitInspectionReportDto report = new SubmitInspectionReportDto(
                InspectionResult.FAILED,
                new BigDecimal("840000.00"),
                "https://insurance.example/inspections/42/damage.jpg",
                "Structural damage found during inspection."
        );

        inspectionService.submitReport(42L, report);

        assertEquals(ProposalStatus.REJECTED, proposal.getStatus());
        assertEquals(BigDecimal.ZERO, inspection.getRecommendedIdv());
        assertEquals(BigDecimal.ZERO, inspection.getApprovedIdv());
        assertEquals(InspectionResult.FAILED, inspection.getResult());
        assertEquals(InspectionStatus.COMPLETED, inspection.getStatus());
        verify(inspectionRepository).save(inspection);
        verify(proposalRepository).save(proposal);
    }

    @Test
    void submitReport_throwsWhenInspectionDoesNotExist() {
        when(inspectionRepository.findByProposalId(404L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> inspectionService.submitReport(
                        404L,
                        new SubmitInspectionReportDto(
                                InspectionResult.PASSED,
                                new BigDecimal("800000.00"),
                                "https://insurance.example/inspections/404/photo.jpg",
                                "Inspection complete."
                        )
                )
        );

        verify(inspectionRepository).findByProposalId(404L);
        verify(inspectionRepository, never()).save(any(Inspection.class));
        verifyNoInteractions(proposalRepository);
    }

    @Test
    void getReportsByInspectorUsername_returnsReportsAndUsesPageRequest() {
        Pageable pageable = PageRequest.of(1, 4);
        when(inspectionRepository.getReportsByInspectorUsername("elena.morris", pageable))
                .thenReturn(List.of(reportResponse));

        List<InspectionResponseDto> response =
                inspectionService.getReportsByInspectorUsername("elena.morris", 1, 4);

        assertEquals(List.of(reportResponse), response);
        verify(inspectionRepository).getReportsByInspectorUsername("elena.morris", pageable);
    }

    @Test
    void getReportsByInspectorUsername_propagatesRepositoryFailure() {
        when(inspectionRepository.getReportsByInspectorUsername(eq("elena.morris"), any(Pageable.class)))
                .thenThrow(new IllegalStateException("report query failed"));

        assertThrows(
                IllegalStateException.class,
                () -> inspectionService.getReportsByInspectorUsername("elena.morris", 0, 5)
        );

        verify(inspectionRepository).getReportsByInspectorUsername("elena.morris", PageRequest.of(0, 5));
    }

    @Test
    void getReportsByInspectorUsername_throwsForInvalidPageArguments() {
        assertThrows(
                IllegalArgumentException.class,
                () -> inspectionService.getReportsByInspectorUsername("elena.morris", -1, 5)
        );

        verifyNoInteractions(inspectionRepository);
    }

    @Test
    void getInspectionsByStatus_returnsMatchingInspections() {
        when(inspectionRepository.getInspectionsByStatus(InspectionStatus.COMPLETED, "elena.morris"))
                .thenReturn(List.of(reportResponse));

        List<InspectionResponseDto> response =
                inspectionService.getInspectionsByStatus(InspectionStatus.COMPLETED, "elena.morris");

        assertEquals(List.of(reportResponse), response);
        verify(inspectionRepository).getInspectionsByStatus(InspectionStatus.COMPLETED, "elena.morris");
    }

    @Test
    void getInspectionsByStatus_propagatesRepositoryFailure() {
        when(inspectionRepository.getInspectionsByStatus(InspectionStatus.COMPLETED, "elena.morris"))
                .thenThrow(new IllegalStateException("inspection query failed"));

        assertThrows(
                IllegalStateException.class,
                () -> inspectionService.getInspectionsByStatus(InspectionStatus.COMPLETED, "elena.morris")
        );

        verify(inspectionRepository).getInspectionsByStatus(InspectionStatus.COMPLETED, "elena.morris");
    }

    @Test
    void getByProposalId_returnsMappedInspectionDetails() {
        when(inspectionRepository.findByProposalId(42L)).thenReturn(Optional.of(inspection));

        InspectionResponseDtoV2 response = inspectionService.getByProposalId(42L);

        assertEquals(detailedResponse, response);
        verify(inspectionRepository).findByProposalId(42L);
    }

    @Test
    void getByProposalId_throwsWhenInspectionDoesNotExist() {
        when(inspectionRepository.findByProposalId(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inspectionService.getByProposalId(404L));

        verify(inspectionRepository).findByProposalId(404L);
    }

    @Test
    void getInspectionsByInspector_returnsMappedInspectionDetails() {
        when(inspectionRepository.findByInspectorId(29L)).thenReturn(List.of(inspection));

        List<InspectionResponseDtoV2> response = inspectionService.getInspectionsByInspector(29L);

        assertEquals(List.of(detailedResponse), response);
        verify(inspectionRepository).findByInspectorId(29L);
    }

    @Test
    void getInspectionsByInspector_propagatesRepositoryFailure() {
        when(inspectionRepository.findByInspectorId(29L))
                .thenThrow(new IllegalStateException("inspection query failed"));

        assertThrows(
                IllegalStateException.class,
                () -> inspectionService.getInspectionsByInspector(29L)
        );

        verify(inspectionRepository).findByInspectorId(29L);
    }
}
