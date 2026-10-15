package com.springboot.AutomobileInsurance.mapper;

import com.springboot.AutomobileInsurance.dto.response.QuoteResponseDto;
import com.springboot.AutomobileInsurance.model.Quote;

import java.math.BigDecimal;

public class QuoteMapper {

    public static QuoteResponseDto convertEntityToDto(Quote quote, BigDecimal idv) {
        return new QuoteResponseDto(
                quote.getId(),
                quote.getProposal().getId(),
                quote.getProposal().getStatus(),
                quote.getProposal().getCustomer().getName(),
                quote.getProposal().getVehicle().getRegistrationNo(),
                quote.getProposal().getPolicyType(),
                idv,
                quote.getTpPremium(),
                quote.getOdPremium(),
                quote.getZeroDepPremium(),
                quote.getEngineProtectPremium(),
                quote.getRoadsideAssistPremium(),
                quote.getNetPremium(),
                quote.getTaxAmount(),
                quote.getTotalPremium(),
                quote.getValidUntil()
        );
    }
}