package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.response.PolicyPaginationDto;
import com.springboot.AutomobileInsurance.dto.response.PolicyResponseDto;
import com.springboot.AutomobileInsurance.dto.response.PolicyResponseDtoV2;
import com.springboot.AutomobileInsurance.enums.PolicyStatus;
import com.springboot.AutomobileInsurance.enums.PolicyType;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.model.Policy;
import com.springboot.AutomobileInsurance.model.Proposal;
import com.springboot.AutomobileInsurance.model.Quote;
import com.springboot.AutomobileInsurance.repository.PolicyRepository;
import com.springboot.AutomobileInsurance.repository.ProposalRepository;
import com.springboot.AutomobileInsurance.repository.QuoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private QuoteRepository quoteRepository;

    @InjectMocks
    private PolicyService policyService;

    private Policy policy;
    private PolicyResponseDto policyResponse;
    private PolicyResponseDtoV2 policyCardResponse;

    @BeforeEach
    void setUp() {
        policy = new Policy();
        policy.setId(64L);
        policy.setPolicyNumber("POL-2026-0064");
        policy.setStartDate(LocalDate.of(2026, 1, 15));
        policy.setEndDate(LocalDate.of(2027, 1, 14));
        policy.setTotalPremium(new BigDecimal("18450.75"));
        policy.setPolicyPdfUrl("https://insurance.example/policies/POL-2026-0064.pdf");
        policy.setStatus(PolicyStatus.ACTIVE);

        policyResponse = new PolicyResponseDto(
                64L,
                "POL-2026-0064",
                "Anika Rao",
                "KA03MN4821",
                "Honda",
                "City",
                PolicyType.COMPREHENSIVE,
                new BigDecimal("850000.00"),
                new BigDecimal("18450.75"),
                LocalDate.of(2026, 1, 15),
                LocalDate.of(2027, 1, 14),
                PolicyStatus.ACTIVE,
                "https://insurance.example/policies/POL-2026-0064.pdf"
        );
        policyCardResponse = new PolicyResponseDtoV2(
                64L,
                "POL-2026-0064",
                "Anika Rao",
                "KA03MN4821",
                "Honda",
                "City",
                PolicyType.COMPREHENSIVE,
                new BigDecimal("850000.00"),
                LocalDate.of(2026, 1, 15),
                LocalDate.of(2027, 1, 14),
                new BigDecimal("18450.75"),
                PolicyStatus.ACTIVE,
                "https://insurance.example/policies/POL-2026-0064.pdf"
        );
    }

    @Test
    void getByPolicyNumber_returnsPolicyResponse() {
        when(policyRepository.findByPolicyNumber("POL-2026-0064")).thenReturn(Optional.of(policy));
        when(policyRepository.getByPolicyNumber("POL-2026-0064")).thenReturn(policyResponse);

        PolicyResponseDto response = policyService.getByPolicyNumber("POL-2026-0064");

        assertEquals(policyResponse, response);
        verify(policyRepository).findByPolicyNumber("POL-2026-0064");
        verify(policyRepository).getByPolicyNumber("POL-2026-0064");
    }

    @Test
    void getByPolicyNumber_throwsWhenPolicyDoesNotExist() {
        when(policyRepository.findByPolicyNumber("POL-404")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> policyService.getByPolicyNumber("POL-404"));

        verify(policyRepository).findByPolicyNumber("POL-404");
        verify(policyRepository, never()).getByPolicyNumber("POL-404");
    }

    @Test
    void getByQuoteId_returnsPolicyResponse() {
        when(quoteRepository.findById(28L)).thenReturn(Optional.of(new Quote()));
        when(policyRepository.getByQuoteId(28L)).thenReturn(policyResponse);

        PolicyResponseDto response = policyService.getByQuoteId(28L);

        assertEquals(policyResponse, response);
        verify(quoteRepository).findById(28L);
        verify(policyRepository).getByQuoteId(28L);
    }

    @Test
    void getByQuoteId_throwsWhenQuoteDoesNotExist() {
        when(quoteRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> policyService.getByQuoteId(404L));

        verify(quoteRepository).findById(404L);
        verify(policyRepository, never()).getByQuoteId(404L);
    }

    @Test
    void getByProposalId_returnsPolicyResponse() {
        when(proposalRepository.findById(82L)).thenReturn(Optional.of(new Proposal()));
        when(policyRepository.getByProposalId(82L)).thenReturn(policyResponse);

        PolicyResponseDto response = policyService.getByProposalId(82L);

        assertEquals(policyResponse, response);
        verify(proposalRepository).findById(82L);
        verify(policyRepository).getByProposalId(82L);
    }

    @Test
    void getByProposalId_throwsWhenProposalDoesNotExist() {
        when(proposalRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> policyService.getByProposalId(404L));

        verify(proposalRepository).findById(404L);
        verify(policyRepository, never()).getByProposalId(404L);
    }

    @Test
    void getAll_returnsPolicyResponses() {
        when(policyRepository.findAllPolicyDto()).thenReturn(List.of(policyResponse));

        List<PolicyResponseDto> response = policyService.getAll();

        assertEquals(List.of(policyResponse), response);
        verify(policyRepository).findAllPolicyDto();
    }

    @Test
    void getAll_propagatesRepositoryFailure() {
        when(policyRepository.findAllPolicyDto()).thenThrow(new IllegalStateException("query failed"));

        assertThrows(IllegalStateException.class, () -> policyService.getAll());

        verify(policyRepository).findAllPolicyDto();
    }

    @Test
    void getMyPolicyCards_returnsCustomerPolicyCards() {
        when(policyRepository.findCustomerPolicyByUsername("anika.rao"))
                .thenReturn(List.of(policyCardResponse));

        List<PolicyResponseDtoV2> response = policyService.getMyPolicyCards("anika.rao");

        assertEquals(List.of(policyCardResponse), response);
        verify(policyRepository).findCustomerPolicyByUsername("anika.rao");
    }

    @Test
    void getMyPolicyCards_propagatesRepositoryFailure() {
        when(policyRepository.findCustomerPolicyByUsername("anika.rao"))
                .thenThrow(new IllegalStateException("query failed"));

        assertThrows(IllegalStateException.class, () -> policyService.getMyPolicyCards("anika.rao"));

        verify(policyRepository).findCustomerPolicyByUsername("anika.rao");
    }

    @Test
    void getAllPoliciesWithPagination_returnsPolicyPageAndPaginationDetails() {
        Pageable pageable = PageRequest.of(1, 2);
        Page<PolicyResponseDto> policyPage = new PageImpl<>(List.of(policyResponse), pageable, 3);
        when(policyRepository.findAllPoliciesPaginated(pageable)).thenReturn(policyPage);

        PolicyPaginationDto response = policyService.getAllPoliciesWithPagination(1, 2);

        assertEquals(List.of(policyResponse), response.list());
        assertEquals(2, response.paginationInfo().totalPages());
        assertEquals(3L, response.paginationInfo().totalRecords());
        assertEquals(1, response.paginationInfo().numRecordsInPage());
        verify(policyRepository).findAllPoliciesPaginated(pageable);
    }

    @Test
    void getAllPoliciesWithPagination_usesFallbacksAndPropagatesRepositoryFailure() {
        Pageable defaultPageable = PageRequest.of(0, 5);
        when(policyRepository.findAllPoliciesPaginated(defaultPageable))
                .thenThrow(new IllegalStateException("query failed"));

        assertThrows(
                IllegalStateException.class,
                () -> policyService.getAllPoliciesWithPagination(-3, 0)
        );

        verify(policyRepository).findAllPoliciesPaginated(defaultPageable);
        verifyNoInteractions(proposalRepository, quoteRepository);
    }
}
