package com.springboot.AutomobileInsurance.dto.response;

import com.springboot.AutomobileInsurance.enums.PolicyType;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record QuoteResponseDto(
        Long quoteId,
        Long proposalId,
        ProposalStatus proposalStatus,
        String customerName,
        String registrationNo,
        PolicyType policyType,
        BigDecimal idv,
        BigDecimal tpPremium,
        BigDecimal odPremium,
        BigDecimal zeroDepPremium,
        BigDecimal engineProtectPremium,
        BigDecimal roadsideAssistPremium,
        BigDecimal netPremium,
        BigDecimal taxAmount,
        BigDecimal totalPremium,
        LocalDate validUntil
) {}