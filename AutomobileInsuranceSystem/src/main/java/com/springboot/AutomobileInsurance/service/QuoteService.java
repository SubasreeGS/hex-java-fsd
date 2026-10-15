package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.response.QuoteResponseDto;
import com.springboot.AutomobileInsurance.enums.InspectionResult;
import com.springboot.AutomobileInsurance.enums.PolicyType;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.mapper.QuoteMapper;
import com.springboot.AutomobileInsurance.model.Inspection;
import com.springboot.AutomobileInsurance.model.Proposal;
import com.springboot.AutomobileInsurance.model.Quote;
import com.springboot.AutomobileInsurance.repository.InspectionRepository;
import com.springboot.AutomobileInsurance.repository.ProposalRepository;
import com.springboot.AutomobileInsurance.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final ProposalRepository proposalRepository;
    private final InspectionRepository inspectionRepository;

    private static final BigDecimal FIXED_TP_RATE = new BigDecimal("3500.00");
    private static final BigDecimal OD_RATE = new BigDecimal("0.03");          // 3% of IDV
    private static final BigDecimal ZERO_DEP_RATE = new BigDecimal("0.015");   // 1.5% of IDV
    private static final BigDecimal ENGINE_PROTECT_RATE = new BigDecimal("0.008"); // 0.8% of IDV
    private static final BigDecimal ROADSIDE_ASSIST_FLAT = new BigDecimal("500.00");
    private static final BigDecimal GST_RATE = new BigDecimal("0.18");        // 18% GST

    public void generateQuote(Long proposalId) {
        if (quoteRepository.existsByProposalId(proposalId)) {
            throw new InvalidCallException("Quote already generated for proposal id: " + proposalId);
        }

        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with id: " + proposalId));

        BigDecimal tpPremium = FIXED_TP_RATE;
        BigDecimal odPremium = BigDecimal.ZERO;
        BigDecimal zeroDepPremium = BigDecimal.ZERO;
        BigDecimal engineProtectPremium = BigDecimal.ZERO;
        BigDecimal roadsideAssistPremium = BigDecimal.ZERO;
        BigDecimal idv = BigDecimal.ZERO;

        if (proposal.getPolicyType() == PolicyType.THIRD_PARTY) {
            odPremium = BigDecimal.ZERO;
        } else {
            if (proposal.getStatus() != ProposalStatus.IDV_APPROVED) {
                throw new InvalidCallException("Quote cannot be generated until the officer approves the IDV. Current proposal status: " + proposal.getStatus());
            }

            Inspection inspection = inspectionRepository.findByProposalId(proposalId)
                    .orElseThrow(() -> new ResourceNotFoundException("Inspection record missing for proposal id: " + proposalId));

            if (inspection.getResult() != InspectionResult.PASSED) {
                throw new InvalidCallException("Cannot generate quote: vehicle inspection did not pass.");
            }

            idv = inspection.getApprovedIdv();
            if (idv == null || idv.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidCallException("Approved IDV must be positive to generate comprehensive quote.");
            }

            odPremium = idv.multiply(OD_RATE).setScale(2, RoundingMode.HALF_UP);

            if (proposal.isZeroDep()) {
                zeroDepPremium = idv.multiply(ZERO_DEP_RATE).setScale(2, RoundingMode.HALF_UP);
            }
            if (proposal.isEngineProtect()) {
                engineProtectPremium = idv.multiply(ENGINE_PROTECT_RATE).setScale(2, RoundingMode.HALF_UP);
            }
            if (proposal.isRoadsideAssist()) {
                roadsideAssistPremium = ROADSIDE_ASSIST_FLAT.setScale(2, RoundingMode.HALF_UP);
            }
        }

        BigDecimal netPremium = tpPremium
                .add(odPremium)
                .add(zeroDepPremium)
                .add(engineProtectPremium)
                .add(roadsideAssistPremium);

        BigDecimal taxAmount = netPremium.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPremium = netPremium.add(taxAmount).setScale(2, RoundingMode.HALF_UP);

        Quote quote = new Quote();
        quote.setProposal(proposal);
        quote.setTpPremium(tpPremium);
        quote.setOdPremium(odPremium);
        quote.setZeroDepPremium(zeroDepPremium);
        quote.setEngineProtectPremium(engineProtectPremium);
        quote.setRoadsideAssistPremium(roadsideAssistPremium);
        quote.setNetPremium(netPremium);
        quote.setTaxAmount(taxAmount);
        quote.setTotalPremium(totalPremium);
        quote.setValidUntil(LocalDate.now().plusDays(15));

        proposal.setStatus(ProposalStatus.QUOTE_GENERATED);
        proposalRepository.save(proposal);

       quoteRepository.save(quote);

    }

    public QuoteResponseDto getByProposalId(Long proposalId) {
        Quote quote = quoteRepository.findByProposalId(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found for proposal id: " + proposalId));

        BigDecimal idv = BigDecimal.ZERO;
        if (quote.getProposal().getPolicyType() == PolicyType.COMPREHENSIVE) {
            idv = inspectionRepository.findByProposalId(proposalId)
                    .map(Inspection::getApprovedIdv)
                    .orElse(BigDecimal.ZERO);
        }

        return QuoteMapper.convertEntityToDto(quote, idv);
    }
}