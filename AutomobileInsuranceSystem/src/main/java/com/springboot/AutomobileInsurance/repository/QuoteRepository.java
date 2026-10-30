package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.dto.response.QuoteResponseDto;
import com.springboot.AutomobileInsurance.model.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {

    Optional<Quote> findByProposalId(Long proposalId);

    boolean existsByProposalId(Long proposalId);


    @Query("""
        SELECT new com.springboot.AutomobileInsurance.dto.response.QuoteResponseDto(
            q.id,
            p.id,
            p.status,
            cust.name,
            v.registrationNo,
            p.policyType,
            CAST(0.00 AS java.math.BigDecimal),
            q.tpPremium,
            q.odPremium,
            q.zeroDepPremium,
            q.engineProtectPremium,
            q.roadsideAssistPremium,
            q.netPremium,
            q.taxAmount,
            q.totalPremium,
            q.validUntil
        )
        FROM Quote q
        JOIN q.proposal p
        JOIN p.customer cust
        JOIN p.vehicle v
        WHERE p.id = ?1
    """)
    QuoteResponseDto getByProposalId(Long proposalId);

    @Query("""
        SELECT new com.springboot.AutomobileInsurance.dto.response.QuoteResponseDto(
            q.id,
            p.id,
            p.status,
            cust.name,
            v.registrationNo,
            p.policyType,
            CAST(0.00 AS java.math.BigDecimal),
            q.tpPremium,
            q.odPremium,
            q.zeroDepPremium,
            q.engineProtectPremium,
            q.roadsideAssistPremium,
            q.netPremium,
            q.taxAmount,
            q.totalPremium,
            q.validUntil
        )
        FROM Quote q
        JOIN q.proposal p
        JOIN p.customer cust
        JOIN p.vehicle v
        WHERE q.id = ?1
    """)
    Optional<QuoteResponseDto> getByQuoteId(Long quoteId);
}