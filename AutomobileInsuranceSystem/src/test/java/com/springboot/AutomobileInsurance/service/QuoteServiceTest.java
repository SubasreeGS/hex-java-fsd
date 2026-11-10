package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.response.QuoteResponseDto;
import com.springboot.AutomobileInsurance.enums.PolicyType;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.model.Inspection;
import com.springboot.AutomobileInsurance.model.Proposal;
import com.springboot.AutomobileInsurance.model.Quote;
import com.springboot.AutomobileInsurance.repository.InspectionRepository;
import com.springboot.AutomobileInsurance.repository.ProposalRepository;
import com.springboot.AutomobileInsurance.repository.QuoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class QuoteServiceTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private InspectionRepository inspectionRepository;

    @InjectMocks
    private QuoteService quoteService;

    private Proposal proposal;
    private Inspection inspection;
    private QuoteResponseDto quoteResponse;

    @BeforeEach
    void setUp() {
        proposal = new Proposal();
        proposal.setId(42L);
        proposal.setPolicyType(PolicyType.COMPREHENSIVE);
        proposal.setStatus(ProposalStatus.IDV_APPROVED);
        proposal.setZeroDep(true);
        proposal.setEngineProtect(true);
        proposal.setRoadsideAssist(true);

        inspection = new Inspection();
        inspection.setId(57L);
        inspection.setProposal(proposal);
        inspection.setApprovedIdv(new BigDecimal("800000.00"));

        quoteResponse = new QuoteResponseDto(
                68L,
                42L,
                ProposalStatus.QUOTE_GENERATED,
                "Anika Rao",
                "KA03MN4821",
                PolicyType.COMPREHENSIVE,
                new BigDecimal("800000.00"),
                new BigDecimal("3500.00"),
                new BigDecimal("24000.00"),
                new BigDecimal("12000.00"),
                new BigDecimal("6400.00"),
                new BigDecimal("500.00"),
                new BigDecimal("46400.00"),
                new BigDecimal("8352.00"),
                new BigDecimal("54752.00"),
                LocalDate.of(2026, 12, 24)
        );
    }

    @Test
    void generateQuote_calculatesComprehensivePremiumWithAllAddOns() {
        when(proposalRepository.findById(42L)).thenReturn(Optional.of(proposal));
        when(inspectionRepository.findByProposalId(42L)).thenReturn(Optional.of(inspection));

        LocalDate expectedValidUntil = LocalDate.now().plusDays(15);
        quoteService.generateQuote(42L);

        ArgumentCaptor<Quote> quoteCaptor = ArgumentCaptor.forClass(Quote.class);
        verify(quoteRepository).save(quoteCaptor.capture());
        Quote savedQuote = quoteCaptor.getValue();
        assertEquals(proposal, savedQuote.getProposal());
        assertEquals(new BigDecimal("3500.00"), savedQuote.getTpPremium());
        assertEquals(new BigDecimal("24000.00"), savedQuote.getOdPremium());
        assertEquals(new BigDecimal("12000.00"), savedQuote.getZeroDepPremium());
        assertEquals(new BigDecimal("6400.00"), savedQuote.getEngineProtectPremium());
        assertEquals(new BigDecimal("500.00"), savedQuote.getRoadsideAssistPremium());
        assertEquals(new BigDecimal("46400.00"), savedQuote.getNetPremium());
        assertEquals(new BigDecimal("8352.00"), savedQuote.getTaxAmount());
        assertEquals(new BigDecimal("54752.00"), savedQuote.getTotalPremium());
        assertEquals(expectedValidUntil, savedQuote.getValidUntil());
        assertEquals(ProposalStatus.QUOTE_GENERATED, proposal.getStatus());
        verify(proposalRepository).save(proposal);
        verify(inspectionRepository).findByProposalId(42L);
    }

    @Test
    void generateQuote_calculatesThirdPartyPremiumWithoutInspection() {
        proposal.setPolicyType(PolicyType.THIRD_PARTY);
        proposal.setZeroDep(true);
        proposal.setEngineProtect(true);
        proposal.setRoadsideAssist(true);
        when(proposalRepository.findById(42L)).thenReturn(Optional.of(proposal));

        quoteService.generateQuote(42L);

        ArgumentCaptor<Quote> quoteCaptor = ArgumentCaptor.forClass(Quote.class);
        verify(quoteRepository).save(quoteCaptor.capture());
        Quote savedQuote = quoteCaptor.getValue();
        assertEquals(new BigDecimal("3500.00"), savedQuote.getTpPremium());
        assertEquals(new BigDecimal("0.00"), savedQuote.getOdPremium());
        assertEquals(new BigDecimal("0.00"), savedQuote.getZeroDepPremium());
        assertEquals(new BigDecimal("0.00"), savedQuote.getEngineProtectPremium());
        assertEquals(new BigDecimal("0.00"), savedQuote.getRoadsideAssistPremium());
        assertEquals(new BigDecimal("3500.00"), savedQuote.getNetPremium());
        assertEquals(new BigDecimal("630.00"), savedQuote.getTaxAmount());
        assertEquals(new BigDecimal("4130.00"), savedQuote.getTotalPremium());
        verifyNoInteractions(inspectionRepository);
        verify(proposalRepository).save(proposal);
    }

    @Test
    void generateQuote_throwsWhenProposalDoesNotExist() {
        when(proposalRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> quoteService.generateQuote(404L));

        verifyNoInteractions(inspectionRepository, quoteRepository);
        verify(proposalRepository, never()).save(any(Proposal.class));
    }

    @Test
    void generateQuote_throwsWhenComprehensiveProposalHasNoInspection() {
        when(proposalRepository.findById(42L)).thenReturn(Optional.of(proposal));
        when(inspectionRepository.findByProposalId(42L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> quoteService.generateQuote(42L));

        verify(inspectionRepository).findByProposalId(42L);
        verifyNoInteractions(quoteRepository);
        verify(proposalRepository, never()).save(any(Proposal.class));
    }

    @Test
    void getByProposalId_returnsQuoteResponse() {
        when(proposalRepository.findById(42L)).thenReturn(Optional.of(proposal));
        when(quoteRepository.getByProposalId(42L)).thenReturn(quoteResponse);

        QuoteResponseDto response = quoteService.getByProposalId(42L);

        assertEquals(quoteResponse, response);
        verify(proposalRepository).findById(42L);
        verify(quoteRepository).getByProposalId(42L);
    }

    @Test
    void getByProposalId_throwsWhenProposalDoesNotExist() {
        when(proposalRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> quoteService.getByProposalId(404L));

        verify(proposalRepository).findById(404L);
        verify(quoteRepository, never()).getByProposalId(404L);
    }
}
