package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.ApproveIdvDto;
import com.springboot.AutomobileInsurance.dto.request.ProposalSubmissionDto;
import com.springboot.AutomobileInsurance.dto.response.ProposalInfoDto;
import com.springboot.AutomobileInsurance.dto.response.ProposalInfoDtoV2;
import com.springboot.AutomobileInsurance.dto.response.ProposalInfoDtoV3;
import com.springboot.AutomobileInsurance.dto.response.ProposalInfoPaginationDto;
import com.springboot.AutomobileInsurance.dto.response.ProposalInfoV3PaginationDto;
import com.springboot.AutomobileInsurance.dto.response.ProposalResponseDto;
import com.springboot.AutomobileInsurance.enums.FuelType;
import com.springboot.AutomobileInsurance.enums.InspectionResult;
import com.springboot.AutomobileInsurance.enums.InspectionStatus;
import com.springboot.AutomobileInsurance.enums.PolicyType;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.model.Customer;
import com.springboot.AutomobileInsurance.model.Inspection;
import com.springboot.AutomobileInsurance.model.Inspector;
import com.springboot.AutomobileInsurance.model.Officer;
import com.springboot.AutomobileInsurance.model.Proposal;
import com.springboot.AutomobileInsurance.model.RtoVehicle;
import com.springboot.AutomobileInsurance.model.User;
import com.springboot.AutomobileInsurance.repository.CustomerRepository;
import com.springboot.AutomobileInsurance.repository.InspectionRepository;
import com.springboot.AutomobileInsurance.repository.InspectorRepository;
import com.springboot.AutomobileInsurance.repository.OfficerRepository;
import com.springboot.AutomobileInsurance.repository.ProposalRepository;
import com.springboot.AutomobileInsurance.repository.RtoVehicleRepository;
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
public class ProposalServiceTest {

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RtoVehicleRepository rtoVehicleRepository;

    @Mock
    private InspectorRepository inspectorRepository;

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private OfficerRepository officerRepository;

    @InjectMocks
    private ProposalService proposalService;

    private Customer customer;
    private RtoVehicle vehicle;
    private Officer officer;
    private Inspector inspector;
    private Proposal proposal;
    private ProposalSubmissionDto submissionDto;
    private ProposalInfoDto proposalInfo;
    private ProposalInfoDtoV2 inspectorProposalInfo;
    private ProposalInfoDtoV3 officerProposalInfo;

    @BeforeEach
    void setUp() {
        User customerUser = new User();
        customerUser.setId(11L);
        customerUser.setUsername("anika.rao");
        customer = new Customer();
        customer.setId(21L);
        customer.setName("Anika Rao");
        customer.setPhoneNo("9876543210");
        customer.setAddress("42 Jacaranda Lane, Bengaluru");
        customer.setDob(LocalDate.of(1991, 6, 12));
        customer.setAadhaarNo("123456789012");
        customer.setPanNo("ABCDE1234F");
        customer.setUser(customerUser);

        vehicle = new RtoVehicle();
        vehicle.setId(31L);
        vehicle.setRegistrationNo("KA03MN4821");
        vehicle.setMake("Honda");
        vehicle.setModel("City");
        vehicle.setManufactureYear(2022);
        vehicle.setEngineCapacity(1498);
        vehicle.setVinNo("MAKAA1234AB567890");
        vehicle.setFuelType(FuelType.PETROL);

        User officerUser = new User();
        officerUser.setId(41L);
        officerUser.setUsername("oliver.grant");
        officer = new Officer();
        officer.setId(51L);
        officer.setName("Oliver Grant");
        officer.setUser(officerUser);

        User inspectorUser = new User();
        inspectorUser.setId(61L);
        inspectorUser.setUsername("elena.morris");
        inspector = new Inspector();
        inspector.setId(71L);
        inspector.setName("Elena Morris");
        inspector.setEmail("elena.morris@example.com");
        inspector.setContact("9123456780");
        inspector.setUser(inspectorUser);
        inspector.setOfficer(officer);

        proposal = new Proposal();
        proposal.setId(81L);
        proposal.setRcDocUrl("https://insurance.example/docs/anika-rc.pdf");
        proposal.setPolicyType(PolicyType.COMPREHENSIVE);
        proposal.setZeroDep(true);
        proposal.setEngineProtect(true);
        proposal.setRoadsideAssist(false);
        proposal.setStatus(ProposalStatus.PROPOSAL_SUBMITTED);
        proposal.setCustomer(customer);
        proposal.setVehicle(vehicle);
        proposal.setOfficer(officer);

        submissionDto = new ProposalSubmissionDto(
                "KA03MN4821",
                "https://insurance.example/docs/anika-rc.pdf",
                PolicyType.COMPREHENSIVE,
                true,
                true,
                false
        );

        proposalInfo = new ProposalInfoDto(
                81L,
                ProposalStatus.PROPOSAL_SUBMITTED,
                PolicyType.COMPREHENSIVE,
                true,
                true,
                false,
                "Elena Morris",
                "elena.morris@example.com",
                "9123456780",
                "KA03MN4821",
                "Honda",
                "City"
        );
        inspectorProposalInfo = new ProposalInfoDtoV2(
                81L,
                "Anika Rao",
                "9876543210",
                "42 Jacaranda Lane, Bengaluru",
                "KA03MN4821",
                "Honda",
                "City",
                2022,
                1498,
                "MAKAA1234AB567890",
                FuelType.PETROL,
                "https://insurance.example/docs/anika-rc.pdf",
                PolicyType.COMPREHENSIVE,
                true,
                true,
                false
        );
        officerProposalInfo = new ProposalInfoDtoV3(
                81L,
                ProposalStatus.PROPOSAL_SUBMITTED,
                PolicyType.COMPREHENSIVE,
                true,
                true,
                false,
                "Anika Rao",
                "9876543210",
                "42 Jacaranda Lane, Bengaluru",
                LocalDate.of(1991, 6, 12),
                "123456789012",
                "ABCDE1234F",
                "KA03MN4821",
                "Honda",
                "City",
                2022,
                FuelType.PETROL,
                "https://insurance.example/docs/anika-rc.pdf",
                "MAKAA1234AB567890",
                1498,
                "Elena Morris"
        );
    }

    @Test
    void submitProposal_savesSubmittedProposal() {
        when(customerRepository.getByCustomerUsername("anika.rao")).thenReturn(customer);
        when(rtoVehicleRepository.findByRegistrationNo("KA03MN4821")).thenReturn(Optional.of(vehicle));

        proposalService.submitProposal(submissionDto, "anika.rao");

        ArgumentCaptor<Proposal> proposalCaptor = ArgumentCaptor.forClass(Proposal.class);
        verify(proposalRepository).save(proposalCaptor.capture());
        Proposal savedProposal = proposalCaptor.getValue();
        assertEquals(customer, savedProposal.getCustomer());
        assertEquals(vehicle, savedProposal.getVehicle());
        assertEquals(ProposalStatus.PROPOSAL_SUBMITTED, savedProposal.getStatus());
        assertEquals(PolicyType.COMPREHENSIVE, savedProposal.getPolicyType());
        assertEquals("https://insurance.example/docs/anika-rc.pdf", savedProposal.getRcDocUrl());
    }

    @Test
    void submitProposal_throwsWhenVehicleRegistrationIsUnknown() {
        when(customerRepository.getByCustomerUsername("anika.rao")).thenReturn(customer);
        when(rtoVehicleRepository.findByRegistrationNo("KA03MN4821")).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> proposalService.submitProposal(submissionDto, "anika.rao")
        );

        verify(proposalRepository, never()).save(any(Proposal.class));
    }

    @Test
    void assignInspector_createsInspectionAndAssignsProposal() {
        when(officerRepository.findByOfficerUsername("oliver.grant")).thenReturn(officer);
        when(inspectorRepository.findById(71L)).thenReturn(Optional.of(inspector));
        when(proposalRepository.findById(81L)).thenReturn(Optional.of(proposal));

        proposalService.assignInspector(71L, 81L, "oliver.grant");

        ArgumentCaptor<Inspection> inspectionCaptor = ArgumentCaptor.forClass(Inspection.class);
        verify(inspectionRepository).save(inspectionCaptor.capture());
        assertEquals(proposal, inspectionCaptor.getValue().getProposal());
        assertEquals(inspector, inspectionCaptor.getValue().getInspector());
        assertEquals(InspectionStatus.SCHEDULED, inspectionCaptor.getValue().getStatus());
        assertEquals(officer, proposal.getOfficer());
        assertEquals(ProposalStatus.INSPECTOR_ASSIGNED, proposal.getStatus());
        verify(proposalRepository).save(proposal);
    }

    @Test
    void assignInspector_throwsWhenInspectorDoesNotExist() {
        when(officerRepository.findByOfficerUsername("oliver.grant")).thenReturn(officer);
        when(inspectorRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> proposalService.assignInspector(404L, 81L, "oliver.grant")
        );

        verifyNoInteractions(proposalRepository, inspectionRepository);
    }

    @Test
    void assignInspector_throwsWhenOfficerDoesNotOwnInspector() {
        when(officerRepository.findByOfficerUsername("someone.else")).thenReturn(new Officer());
        when(inspectorRepository.findById(71L)).thenReturn(Optional.of(inspector));

        assertThrows(
                InvalidCallException.class,
                () -> proposalService.assignInspector(71L, 81L, "someone.else")
        );

        verifyNoInteractions(proposalRepository, inspectionRepository);
    }

    @Test
    void assignInspector_throwsWhenProposalDoesNotExist() {
        when(officerRepository.findByOfficerUsername("oliver.grant")).thenReturn(officer);
        when(inspectorRepository.findById(71L)).thenReturn(Optional.of(inspector));
        when(proposalRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> proposalService.assignInspector(71L, 404L, "oliver.grant")
        );

        verify(inspectionRepository, never()).save(any(Inspection.class));
        verify(proposalRepository, never()).save(any(Proposal.class));
    }

    @Test
    void approveIdv_updatesInspectionAndProposal() {
        Inspection inspection = new Inspection();
        inspection.setId(91L);
        inspection.setProposal(proposal);
        inspection.setInspector(inspector);
        inspection.setResult(InspectionResult.PASSED);
        when(inspectionRepository.findByProposalId(81L)).thenReturn(Optional.of(inspection));
        BigDecimal approvedIdv = new BigDecimal("825000.00");

        proposalService.approveIdv(81L, new ApproveIdvDto(approvedIdv), "oliver.grant");

        assertEquals(approvedIdv, inspection.getApprovedIdv());
        assertEquals(ProposalStatus.IDV_APPROVED, proposal.getStatus());
        verify(inspectionRepository).save(inspection);
        verify(proposalRepository).save(proposal);
    }

    @Test
    void approveIdv_throwsWhenInspectionDoesNotExist() {
        when(inspectionRepository.findByProposalId(404L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> proposalService.approveIdv(404L, new ApproveIdvDto(new BigDecimal("825000.00")), "oliver.grant")
        );

        verify(inspectionRepository, never()).save(any(Inspection.class));
        verify(proposalRepository, never()).save(any(Proposal.class));
    }

    @Test
    void approveIdv_throwsWhenOfficerDoesNotOwnInspector() {
        Inspection inspection = new Inspection();
        inspection.setProposal(proposal);
        inspection.setInspector(inspector);
        inspection.setResult(InspectionResult.PASSED);
        when(inspectionRepository.findByProposalId(81L)).thenReturn(Optional.of(inspection));

        assertThrows(
                InvalidCallException.class,
                () -> proposalService.approveIdv(81L, new ApproveIdvDto(new BigDecimal("825000.00")), "unauthorized")
        );

        verify(inspectionRepository, never()).save(any(Inspection.class));
        verify(proposalRepository, never()).save(any(Proposal.class));
    }

    @Test
    void approveIdv_throwsWhenInspectionDidNotPass() {
        Inspection inspection = new Inspection();
        inspection.setProposal(proposal);
        inspection.setInspector(inspector);
        inspection.setResult(InspectionResult.FAILED);
        when(inspectionRepository.findByProposalId(81L)).thenReturn(Optional.of(inspection));

        assertThrows(
                InvalidCallException.class,
                () -> proposalService.approveIdv(81L, new ApproveIdvDto(new BigDecimal("825000.00")), "oliver.grant")
        );

        verify(inspectionRepository, never()).save(any(Inspection.class));
        verify(proposalRepository, never()).save(any(Proposal.class));
    }

    @Test
    void getProposalByCustomerId_returnsCustomerProposals() {
        when(customerRepository.findById(21L)).thenReturn(Optional.of(customer));
        when(proposalRepository.getProposalByCustomerId(eq(21L), eq(PageRequest.of(0, 5))))
                .thenReturn(List.of(proposalInfo));

        List<ProposalInfoDto> response = proposalService.getProposalByCustomerId(21L, 0, 5);

        assertEquals(List.of(proposalInfo), response);
        verify(customerRepository).findById(21L);
        verify(proposalRepository).getProposalByCustomerId(21L, PageRequest.of(0, 5));
    }

    @Test
    void getProposalByCustomerId_throwsWhenCustomerDoesNotExist() {
        when(customerRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> proposalService.getProposalByCustomerId(404L, 0, 5)
        );

        verify(proposalRepository, never()).getProposalByCustomerId(eq(404L), any(Pageable.class));
    }

    @Test
    void getByCustomerUsername_returnsCustomerProposals() {
        when(proposalRepository.getByCustomerUsername("anika.rao", PageRequest.of(1, 4)))
                .thenReturn(List.of(proposalInfo));

        List<ProposalInfoDto> response = proposalService.getByCustomerUsername(1, 4, "anika.rao");

        assertEquals(List.of(proposalInfo), response);
        verify(proposalRepository).getByCustomerUsername("anika.rao", PageRequest.of(1, 4));
    }

    @Test
    void getByCustomerUsername_propagatesRepositoryFailure() {
        when(proposalRepository.getByCustomerUsername(eq("anika.rao"), any(Pageable.class)))
                .thenThrow(new IllegalStateException("query failed"));

        assertThrows(
                IllegalStateException.class,
                () -> proposalService.getByCustomerUsername(0, 5, "anika.rao")
        );

        verify(proposalRepository).getByCustomerUsername("anika.rao", PageRequest.of(0, 5));
    }

    @Test
    void getByInspectorUsername_returnsPaginatedProposals() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<ProposalInfoDtoV2> page = new PageImpl<>(List.of(inspectorProposalInfo), pageable, 7);
        when(proposalRepository.getByInspectorUsername("elena.morris", pageable)).thenReturn(page);

        ProposalInfoPaginationDto response = proposalService.getByInspectorUsername(0, 5, "elena.morris");

        assertEquals(List.of(inspectorProposalInfo), response.list());
        assertEquals(2, response.paginationInfo().totalPages());
        assertEquals(7L, response.paginationInfo().totalRecords());
        assertEquals(1, response.paginationInfo().numRecordsInPage());
        verify(proposalRepository).getByInspectorUsername("elena.morris", pageable);
    }

    @Test
    void getByInspectorUsername_propagatesRepositoryFailure() {
        when(proposalRepository.getByInspectorUsername(eq("elena.morris"), any(Pageable.class)))
                .thenThrow(new IllegalStateException("query failed"));

        assertThrows(
                IllegalStateException.class,
                () -> proposalService.getByInspectorUsername(0, 5, "elena.morris")
        );

        verify(proposalRepository).getByInspectorUsername("elena.morris", PageRequest.of(0, 5));
    }

    @Test
    void getByOfficerUsername_returnsOfficerProposals() {
        when(proposalRepository.getByOfficerUsername("oliver.grant", PageRequest.of(0, 5)))
                .thenReturn(List.of(officerProposalInfo));

        List<ProposalInfoDtoV3> response = proposalService.getByOfficerUsername(0, 5, "oliver.grant");

        assertEquals(List.of(officerProposalInfo), response);
        verify(proposalRepository).getByOfficerUsername("oliver.grant", PageRequest.of(0, 5));
    }

    @Test
    void getByOfficerUsername_propagatesRepositoryFailure() {
        when(proposalRepository.getByOfficerUsername(eq("oliver.grant"), any(Pageable.class)))
                .thenThrow(new IllegalStateException("query failed"));

        assertThrows(
                IllegalStateException.class,
                () -> proposalService.getByOfficerUsername(0, 5, "oliver.grant")
        );

        verify(proposalRepository).getByOfficerUsername("oliver.grant", PageRequest.of(0, 5));
    }

    @Test
    void getByProposalStatus_returnsPaginatedProposals() {
        Pageable pageable = PageRequest.of(1, 4);
        Page<ProposalInfoDtoV3> page = new PageImpl<>(List.of(officerProposalInfo), pageable, 5);
        when(proposalRepository.getByProposalStatus(ProposalStatus.PROPOSAL_SUBMITTED, "oliver.grant", pageable))
                .thenReturn(page);

        ProposalInfoV3PaginationDto response = proposalService.getByProposalStatus(
                ProposalStatus.PROPOSAL_SUBMITTED, 1, 4, "oliver.grant"
        );

        assertEquals(List.of(officerProposalInfo), response.list());
        assertEquals(2, response.paginationInfo().totalPages());
        assertEquals(5L, response.paginationInfo().totalRecords());
        assertEquals(1, response.paginationInfo().numRecordsInPage());
        verify(proposalRepository).getByProposalStatus(
                ProposalStatus.PROPOSAL_SUBMITTED, "oliver.grant", pageable
        );
    }

    @Test
    void getByProposalStatus_propagatesRepositoryFailure() {
        when(proposalRepository.getByProposalStatus(
                eq(ProposalStatus.PROPOSAL_SUBMITTED), eq("oliver.grant"), any(Pageable.class)
        )).thenThrow(new IllegalStateException("query failed"));

        assertThrows(
                IllegalStateException.class,
                () -> proposalService.getByProposalStatus(
                        ProposalStatus.PROPOSAL_SUBMITTED, 0, 5, "oliver.grant"
                )
        );

        verify(proposalRepository).getByProposalStatus(
                ProposalStatus.PROPOSAL_SUBMITTED, "oliver.grant", PageRequest.of(0, 5)
        );
    }

    @Test
    void getById_returnsProposalResponse() {
        when(proposalRepository.findById(81L)).thenReturn(Optional.of(proposal));

        ProposalResponseDto response = proposalService.getById(81L);

        assertEquals(81L, response.id());
        assertEquals(21L, response.customerId());
        assertEquals("Anika Rao", response.customerName());
        assertEquals(31L, response.vehicleId());
        assertEquals("Oliver Grant", response.officerName());
        verify(proposalRepository).findById(81L);
    }

    @Test
    void getById_throwsWhenProposalDoesNotExist() {
        when(proposalRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> proposalService.getById(404L));

        verify(proposalRepository).findById(404L);
    }

    @Test
    void getAll_returnsMappedProposalResponses() {
        when(proposalRepository.findAll()).thenReturn(List.of(proposal));

        List<ProposalResponseDto> response = proposalService.getAll();

        assertEquals(1, response.size());
        assertEquals(81L, response.get(0).id());
        assertEquals("KA03MN4821", response.get(0).registrationNo());
        verify(proposalRepository).findAll();
    }

    @Test
    void getAll_propagatesRepositoryFailure() {
        when(proposalRepository.findAll()).thenThrow(new IllegalStateException("query failed"));

        assertThrows(IllegalStateException.class, () -> proposalService.getAll());

        verify(proposalRepository).findAll();
    }

    @Test
    void getByOfficerId_returnsMappedProposalsForOfficer() {
        when(proposalRepository.findByOfficerId(51L)).thenReturn(List.of(proposal));

        List<ProposalResponseDto> response = proposalService.getByOfficerId(51L);

        assertEquals(1, response.size());
        assertEquals(81L, response.get(0).id());
        assertEquals(51L, response.get(0).officerId());
        verify(proposalRepository).findByOfficerId(51L);
    }

    @Test
    void getByOfficerId_propagatesRepositoryFailure() {
        when(proposalRepository.findByOfficerId(51L)).thenThrow(new IllegalStateException("query failed"));

        assertThrows(IllegalStateException.class, () -> proposalService.getByOfficerId(51L));

        verify(proposalRepository).findByOfficerId(51L);
    }
}
