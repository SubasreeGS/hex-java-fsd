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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final ProposalRepository proposalRepository;
    private final InspectionRepository inspectionRepository;

    // Standard insurance premium rates
    private final BigDecimal fixedTpRate = BigDecimal.valueOf(3500);
    private final BigDecimal odRate = BigDecimal.valueOf(0.03);
    private final BigDecimal zeroDepRate = BigDecimal.valueOf(0.015);
    private final BigDecimal engineProtectRate = BigDecimal.valueOf(0.008);
    private final BigDecimal roadsideAssistFlat = BigDecimal.valueOf(500);
    private final BigDecimal gstRate = BigDecimal.valueOf(0.18);

    @Transactional
    public void generateQuote(Long proposalId)
    {
        // Fetch Proposal object from DB using proposal id
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with id: " + proposalId));

        // Set default starting values for all premiums
        BigDecimal tpPremium = fixedTpRate;
        BigDecimal odPremium = BigDecimal.ZERO;
        BigDecimal zeroDepPremium = BigDecimal.ZERO;
        BigDecimal engineProtectPremium = BigDecimal.ZERO;
        BigDecimal roadsideAssistPremium = BigDecimal.ZERO;
        BigDecimal idv = BigDecimal.ZERO;


        // Calculate premiums based on policy type
        if (proposal.getPolicyType() == PolicyType.THIRD_PARTY)
        {
            // Third party policy covers only third party damages, so own damage is zero
            odPremium = BigDecimal.ZERO;
        }

        else // For comprehensive policy,
        {

            // fetch inspection details to get approved IDV
            Inspection inspection = inspectionRepository.findByProposalId(proposalId)
                    .orElseThrow(() -> new ResourceNotFoundException("Inspection record not found"));


            idv = inspection.getApprovedIdv();

            // Calculate own damage premium based on approved vehicle value
            odPremium = idv.multiply(odRate);

            // Add extra charges if the customer choose addons
            if (proposal.isZeroDep()) {
                zeroDepPremium = idv.multiply(zeroDepRate);
            }
            if (proposal.isEngineProtect()) {
                engineProtectPremium = idv.multiply(engineProtectRate);
            }
            if (proposal.isRoadsideAssist()) {
                roadsideAssistPremium = roadsideAssistFlat;
            }
        }

        // Sum all individual covers to get net premium before taxes
        BigDecimal netPremium = tpPremium
                .add(odPremium)
                .add(zeroDepPremium)
                .add(engineProtectPremium)
                .add(roadsideAssistPremium);

        // Calculate GST (18%) and final total amount
        BigDecimal taxAmount = netPremium.multiply(gstRate);
        BigDecimal totalPremium = netPremium.add(taxAmount);

        // Build the Quote entity and format all values to 2 decimal places
        Quote quote = new Quote();
        quote.setProposal(proposal);
        quote.setTpPremium(round(tpPremium));
        quote.setOdPremium(round(odPremium));
        quote.setZeroDepPremium(round(zeroDepPremium));
        quote.setEngineProtectPremium(round(engineProtectPremium));
        quote.setRoadsideAssistPremium(round(roadsideAssistPremium));
        quote.setNetPremium(round(netPremium));
        quote.setTaxAmount(round(taxAmount));
        quote.setTotalPremium(round(totalPremium));

        // Quote expires in 15 days
        quote.setValidUntil(LocalDate.now().plusDays(15));

        // Update proposal status and save both records to db
        proposal.setStatus(ProposalStatus.QUOTE_GENERATED);
        proposalRepository.save(proposal);

        quoteRepository.save(quote);
    }

    @Transactional
    public QuoteResponseDto getByProposalId(Long proposalId) {
        Proposal proposal=proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found"));

        return quoteRepository.getByProposalId(proposalId);
    }

    private BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}