package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.InspectorDto;
import com.springboot.AutomobileInsurance.dto.request.InspectorUpdateDto;
import com.springboot.AutomobileInsurance.dto.response.InspectorClaimFilterDto;
import com.springboot.AutomobileInsurance.dto.response.InspectorClaimRespDto;
import com.springboot.AutomobileInsurance.dto.response.InspectorDropdownDto;
import com.springboot.AutomobileInsurance.dto.response.InspectorMetricsDto;
import com.springboot.AutomobileInsurance.dto.response.InspectorPaginationDto;
import com.springboot.AutomobileInsurance.dto.response.InspectorProposalFilterDto;
import com.springboot.AutomobileInsurance.dto.response.InspectorProposalRespDto;
import com.springboot.AutomobileInsurance.dto.response.InspectorResponseDto;
import com.springboot.AutomobileInsurance.enums.ClaimStatus;
import com.springboot.AutomobileInsurance.enums.InspectionStatus;
import com.springboot.AutomobileInsurance.enums.PolicyType;
import com.springboot.AutomobileInsurance.enums.Role;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.model.Inspector;
import com.springboot.AutomobileInsurance.model.Officer;
import com.springboot.AutomobileInsurance.model.User;
import com.springboot.AutomobileInsurance.repository.ClaimRepository;
import com.springboot.AutomobileInsurance.repository.InspectionRepository;
import com.springboot.AutomobileInsurance.repository.InspectorRepository;
import com.springboot.AutomobileInsurance.repository.OfficerRepository;
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
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InspectorServiceTest {

    @Mock
    private InspectorRepository inspectorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OfficerRepository officerRepository;

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private ClaimRepository claimRepository;

    @InjectMocks
    private InspectorService inspectorService;

    private InspectorDto inspectorDto;
    private Inspector inspector;
    private User inspectorUser;
    private Officer officer;
    private InspectorDropdownDto dropdownDto;
    private InspectorProposalRespDto proposalDto;
    private InspectorClaimRespDto claimDto;

    @BeforeEach
    void setUp() {
        inspectorDto = new InspectorDto(
                "elena.morris",
                "Inspect!2026",
                "Elena Morris",
                "elena.morris@example.com",
                "9876543210"
        );

        User officerUser = new User();
        officerUser.setId(7L);
        officerUser.setUsername("oliver.grant");
        officerUser.setPassword("officer-password");
        officerUser.setRole(Role.OFFICER);
        officer = new Officer();
        officer.setId(12L);
        officer.setName("Oliver Grant");
        officer.setUser(officerUser);

        inspectorUser = new User();
        inspectorUser.setId(25L);
        inspectorUser.setUsername("elena.morris");
        inspectorUser.setPassword("encoded-password");
        inspectorUser.setRole(Role.INSPECTOR);

        inspector = new Inspector();
        inspector.setId(31L);
        inspector.setName("Elena Morris");
        inspector.setEmail("elena.morris@example.com");
        inspector.setContact("9876543210");
        inspector.setUser(inspectorUser);
        inspector.setOfficer(officer);

        dropdownDto = new InspectorDropdownDto(31L, "Elena Morris", "9876543210");
        proposalDto = new InspectorProposalRespDto(
                81L,
                91L,
                "Priya Nair",
                "MH12AB3456",
                PolicyType.COMPREHENSIVE,
                InspectionStatus.SCHEDULED,
                Instant.parse("2026-03-10T09:30:00Z")
        );
        claimDto = new InspectorClaimRespDto(
                101L,
                "CLM-2026-0101",
                "Priya Nair",
                "MH12AB3456",
                new BigDecimal("42500.00"),
                ClaimStatus.UNDER_ASSESSMENT,
                LocalDate.of(2026, 3, 8)
        );
    }

    @Test
    void insertInspector_savesUserAndInspectorAndReturnsResponse() {
        when(userRepository.existsByUsername("elena.morris")).thenReturn(false);
        when(officerRepository.findByOfficerUsername("oliver.grant")).thenReturn(officer);
        when(passwordEncoder.encode("Inspect!2026")).thenReturn("hashed-inspector-password");
        when(inspectorRepository.save(any(Inspector.class))).thenReturn(inspector);

        InspectorResponseDto response = inspectorService.insertInspector(inspectorDto, "oliver.grant");

        assertEquals(31L, response.id());
        assertEquals("Elena Morris", response.name());
        assertEquals("elena.morris", response.username());
        assertEquals("oliver.grant", response.officerUsername());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Inspector> inspectorCaptor = ArgumentCaptor.forClass(Inspector.class);
        verify(userRepository).save(userCaptor.capture());
        verify(inspectorRepository).save(inspectorCaptor.capture());
        assertEquals("hashed-inspector-password", userCaptor.getValue().getPassword());
        assertEquals(Role.INSPECTOR, userCaptor.getValue().getRole());
        assertEquals(userCaptor.getValue(), inspectorCaptor.getValue().getUser());
        assertEquals(officer, inspectorCaptor.getValue().getOfficer());
    }

    @Test
    void insertInspector_throwsWhenUsernameAlreadyExists() {
        when(userRepository.existsByUsername("elena.morris")).thenReturn(true);

        assertThrows(
                RuntimeException.class,
                () -> inspectorService.insertInspector(inspectorDto, "oliver.grant")
        );

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder, officerRepository, inspectorRepository);
    }

    @Test
    void insertInspector_propagatesRepositoryFailure() {
        when(userRepository.existsByUsername("elena.morris")).thenReturn(false);
        when(officerRepository.findByOfficerUsername("oliver.grant")).thenReturn(officer);
        when(passwordEncoder.encode("Inspect!2026")).thenReturn("hashed-inspector-password");
        when(inspectorRepository.save(any(Inspector.class))).thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(
                IllegalStateException.class,
                () -> inspectorService.insertInspector(inspectorDto, "oliver.grant")
        );

        verify(userRepository).save(any(User.class));
        verify(inspectorRepository).save(any(Inspector.class));
    }

    @Test
    void getAllInspectorsForDropdown_returnsDropdownEntries() {
        when(inspectorRepository.findAllForDropdown()).thenReturn(List.of(dropdownDto));

        List<InspectorDropdownDto> response = inspectorService.getAllInspectorsForDropdown();

        assertEquals(List.of(dropdownDto), response);
        verify(inspectorRepository).findAllForDropdown();
    }

    @Test
    void getAllInspectorsForDropdown_propagatesRepositoryFailure() {
        when(inspectorRepository.findAllForDropdown()).thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(IllegalStateException.class, () -> inspectorService.getAllInspectorsForDropdown());

        verify(inspectorRepository).findAllForDropdown();
    }

    @Test
    void getById_returnsInspectorResponse() {
        when(inspectorRepository.findById(31L)).thenReturn(Optional.of(inspector));

        InspectorResponseDto response = inspectorService.getById(31L);

        assertEquals(31L, response.id());
        assertEquals("elena.morris", response.username());
        assertEquals("oliver.grant", response.officerUsername());
        verify(inspectorRepository).findById(31L);
    }

    @Test
    void getById_throwsWhenInspectorDoesNotExist() {
        when(inspectorRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inspectorService.getById(404L));

        verify(inspectorRepository).findById(404L);
    }

    @Test
    void update_changesTrimmedFieldsAndReturnsResponse() {
        when(inspectorRepository.findById(31L)).thenReturn(Optional.of(inspector));
        when(inspectorRepository.save(inspector)).thenReturn(inspector);

        InspectorResponseDto response = inspectorService.update(
                31L,
                new InspectorUpdateDto("  Elena M. Morris  ", "  elena.m@example.com  ", " 9123456780 ")
        );

        assertEquals("Elena M. Morris", response.name());
        assertEquals("elena.m@example.com", response.email());
        assertEquals("9123456780", response.contact());
        verify(inspectorRepository).save(inspector);
    }

    @Test
    void update_throwsWhenInspectorDoesNotExist() {
        when(inspectorRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> inspectorService.update(404L, new InspectorUpdateDto("New Name", null, null))
        );

        verify(inspectorRepository, never()).save(any(Inspector.class));
    }

    @Test
    void update_throwsWhenNoUsableFieldsAreProvided() {
        when(inspectorRepository.findById(31L)).thenReturn(Optional.of(inspector));

        assertThrows(
                InvalidCallException.class,
                () -> inspectorService.update(31L, new InspectorUpdateDto("  ", null, null))
        );

        verify(inspectorRepository, never()).save(any(Inspector.class));
    }

    @Test
    void deleteById_deletesInspectorAndLinkedUser() {
        when(inspectorRepository.findById(31L)).thenReturn(Optional.of(inspector));

        inspectorService.deleteById(31L);

        verify(inspectorRepository).delete(inspector);
        verify(userRepository).deleteById(25L);
    }

    @Test
    void deleteById_throwsWhenInspectorDoesNotExist() {
        when(inspectorRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inspectorService.deleteById(404L));

        verify(inspectorRepository, never()).delete(any(Inspector.class));
        verifyNoInteractions(userRepository);
    }

    @Test
    void getAll_returnsInspectorResponses() {
        when(inspectorRepository.findAll()).thenReturn(List.of(inspector));

        List<InspectorResponseDto> response = inspectorService.getAll();

        assertEquals(1, response.size());
        assertEquals("Elena Morris", response.get(0).name());
        assertEquals("oliver.grant", response.get(0).officerUsername());
        verify(inspectorRepository).findAll();
    }

    @Test
    void getAll_propagatesRepositoryFailure() {
        when(inspectorRepository.findAll()).thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(IllegalStateException.class, () -> inspectorService.getAll());

        verify(inspectorRepository).findAll();
    }

    @Test
    void filterInspectorProposals_returnsContentAndPagination() {
        LocalDate fromDate = LocalDate.of(2026, 3, 1);
        LocalDate toDate = LocalDate.of(2026, 3, 31);
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 5);
        Page<InspectorProposalRespDto> proposalPage = new PageImpl<>(List.of(proposalDto), pageable, 7);
        when(inspectionRepository.dynamicFilterInspectorProposals(
                31L,
                PolicyType.COMPREHENSIVE,
                InspectionStatus.SCHEDULED,
                fromDate.atStartOfDay(java.time.ZoneOffset.UTC).toInstant(),
                toDate.atTime(java.time.LocalTime.MAX).atOffset(java.time.ZoneOffset.UTC).toInstant(),
                pageable
        )).thenReturn(proposalPage);

        InspectorProposalFilterDto response = inspectorService.filterInspectorProposals(
                31L,
                PolicyType.COMPREHENSIVE,
                InspectionStatus.SCHEDULED,
                fromDate,
                toDate,
                0,
                5
        );

        assertEquals(List.of(proposalDto), response.list());
        assertEquals(2, response.paginationInfo().totalPages());
        assertEquals(7, response.paginationInfo().totalRecords());
        assertEquals(1, response.paginationInfo().numRecordsInPage());
        verify(inspectionRepository).dynamicFilterInspectorProposals(
                31L,
                PolicyType.COMPREHENSIVE,
                InspectionStatus.SCHEDULED,
                fromDate.atStartOfDay(java.time.ZoneOffset.UTC).toInstant(),
                toDate.atTime(java.time.LocalTime.MAX).atOffset(java.time.ZoneOffset.UTC).toInstant(),
                pageable
        );
    }

    @Test
    void filterInspectorProposals_usesDefaultsAndPropagatesRepositoryFailure() {
        when(inspectionRepository.dynamicFilterInspectorProposals(
                31L, null, null, null, null, org.springframework.data.domain.PageRequest.of(0, 10)
        )).thenThrow(new IllegalStateException("query failed"));

        assertThrows(
                IllegalStateException.class,
                () -> inspectorService.filterInspectorProposals(31L, null, null, null, null, -1, 0)
        );

        verify(inspectionRepository).dynamicFilterInspectorProposals(
                31L, null, null, null, null, org.springframework.data.domain.PageRequest.of(0, 10)
        );
    }

    @Test
    void filterInspectorClaims_returnsContentAndPagination() {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(1, 4);
        Page<InspectorClaimRespDto> claimPage = new PageImpl<>(List.of(claimDto), pageable, 5);
        when(claimRepository.dynamicFilterInspectorClaims(
                ClaimStatus.UNDER_ASSESSMENT,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31),
                pageable
        )).thenReturn(claimPage);

        InspectorClaimFilterDto response = inspectorService.filterInspectorClaims(
                ClaimStatus.UNDER_ASSESSMENT,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31),
                1,
                4
        );

        assertEquals(List.of(claimDto), response.list());
        assertEquals(2, response.paginationInfo().totalPages());
        assertEquals(5, response.paginationInfo().totalRecords());
        assertEquals(1, response.paginationInfo().numRecordsInPage());
        verify(claimRepository).dynamicFilterInspectorClaims(
                ClaimStatus.UNDER_ASSESSMENT,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31),
                pageable
        );
    }

    @Test
    void filterInspectorClaims_usesDefaultsAndPropagatesRepositoryFailure() {
        Pageable defaultPageable = org.springframework.data.domain.PageRequest.of(0, 10);
        when(claimRepository.dynamicFilterInspectorClaims(null, null, null, defaultPageable))
                .thenThrow(new IllegalStateException("query failed"));

        assertThrows(
                IllegalStateException.class,
                () -> inspectorService.filterInspectorClaims(null, null, null, -1, 0)
        );

        verify(claimRepository).dynamicFilterInspectorClaims(null, null, null, defaultPageable);
    }

    @Test
    void getInspectorMetrics_returnsCountsFromRepositories() {
        when(inspectionRepository.countPendingSurveysByInspector(31L)).thenReturn(3L);
        when(inspectionRepository.countCompletedSurveysByInspector(31L)).thenReturn(8L);
        when(claimRepository.countPendingClaimAssessments()).thenReturn(2L);

        InspectorMetricsDto response = inspectorService.getInspectorMetrics(31L);

        assertEquals(3L, response.pendingVehicleSurveys());
        assertEquals(2L, response.pendingClaimSurveys());
        assertEquals(8L, response.completedSurveys());
        verify(inspectionRepository).countPendingSurveysByInspector(31L);
        verify(inspectionRepository).countCompletedSurveysByInspector(31L);
        verify(claimRepository).countPendingClaimAssessments();
    }

    @Test
    void getInspectorMetrics_propagatesRepositoryFailure() {
        when(inspectionRepository.countPendingSurveysByInspector(31L))
                .thenThrow(new IllegalStateException("count query failed"));

        assertThrows(IllegalStateException.class, () -> inspectorService.getInspectorMetrics(31L));

        verify(inspectionRepository).countPendingSurveysByInspector(31L);
        verifyNoInteractions(claimRepository);
    }

    @Test
    void getByUsername_returnsInspectorResponse() {
        when(inspectorRepository.findByUserUsername("elena.morris")).thenReturn(Optional.of(inspector));

        InspectorResponseDto response = inspectorService.getByUsername("elena.morris");

        assertEquals(31L, response.id());
        assertEquals("elena.morris", response.username());
        verify(inspectorRepository).findByUserUsername("elena.morris");
    }

    @Test
    void getByUsername_throwsWhenInspectorDoesNotExist() {
        when(inspectorRepository.findByUserUsername("unknown.inspector")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inspectorService.getByUsername("unknown.inspector"));

        verify(inspectorRepository).findByUserUsername("unknown.inspector");
    }

    @Test
    void getInspectorsWithPagination_returnsContentAndPagination() {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 3);
        Page<Inspector> inspectorPage = new PageImpl<>(List.of(inspector), pageable, 4);
        when(inspectorRepository.findAll(pageable)).thenReturn(inspectorPage);

        InspectorPaginationDto response = inspectorService.getInspectorsWithPagination(-2, 3);

        assertEquals(1, response.list().size());
        assertEquals("Elena Morris", response.list().get(0).name());
        assertEquals(2, response.paginationInfo().totalPages());
        assertEquals(4, response.paginationInfo().totalRecords());
        assertEquals(1, response.paginationInfo().numRecordsInPage());
        verify(inspectorRepository).findAll(pageable);
    }

    @Test
    void getInspectorsWithPagination_usesFallbackSizeAndPropagatesRepositoryFailure() {
        Pageable defaultPageable = org.springframework.data.domain.PageRequest.of(0, 5);
        when(inspectorRepository.findAll(defaultPageable)).thenThrow(new IllegalStateException("query failed"));

        assertThrows(
                IllegalStateException.class,
                () -> inspectorService.getInspectorsWithPagination(-1, 0)
        );

        verify(inspectorRepository).findAll(defaultPageable);
    }

    @Test
    void getMyInspectorsForDropdown_returnsOfficerInspectors() {
        when(inspectorRepository.findMyInspectorsForDropdown("oliver.grant")).thenReturn(List.of(dropdownDto));

        List<InspectorDropdownDto> response = inspectorService.getMyInspectorsForDropdown("oliver.grant");

        assertEquals(List.of(dropdownDto), response);
        verify(inspectorRepository).findMyInspectorsForDropdown("oliver.grant");
    }

    @Test
    void getMyInspectorsForDropdown_propagatesRepositoryFailure() {
        when(inspectorRepository.findMyInspectorsForDropdown("oliver.grant"))
                .thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(
                IllegalStateException.class,
                () -> inspectorService.getMyInspectorsForDropdown("oliver.grant")
        );

        verify(inspectorRepository).findMyInspectorsForDropdown("oliver.grant");
    }
}
