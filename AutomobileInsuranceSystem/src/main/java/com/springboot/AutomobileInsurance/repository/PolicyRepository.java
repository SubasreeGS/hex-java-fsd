package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.dto.response.LedgerRespDto;
import com.springboot.AutomobileInsurance.dto.response.PolicyResponseDto;
import com.springboot.AutomobileInsurance.dto.response.PolicyResponseDtoV2;
import com.springboot.AutomobileInsurance.enums.PolicyStatus;
import com.springboot.AutomobileInsurance.model.Policy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    Optional<Policy> findByPolicyNumber(String policyNumber);


    @Query("""
    SELECT new com.springboot.AutomobileInsurance.dto.response.PolicyResponseDto(
        pol.id,
        pol.policyNumber,
        cust.name,
        v.registrationNo,
        v.make,
        v.model,
        prop.policyType,
        i.approvedIdv,
        pol.totalPremium,
        pol.startDate,
        pol.endDate,
        pol.status,
        pol.policyPdfUrl
    )
    FROM Policy pol
    JOIN pol.payment pay
    JOIN pay.quote q
    JOIN q.proposal prop
    JOIN prop.customer cust
    JOIN prop.vehicle v
    LEFT JOIN Inspection i ON i.proposal = prop
    WHERE pol.policyNumber = ?1
""")
    PolicyResponseDto getByPolicyNumber(String policyNumber);


    @Query("""
    SELECT new com.springboot.AutomobileInsurance.dto.response.PolicyResponseDto(
        pol.id,
        pol.policyNumber,
        cust.name,
        v.registrationNo,
        v.make,
        v.model,
        prop.policyType,
       i.approvedIdv,
        pol.totalPremium,
        pol.startDate,
        pol.endDate,
        pol.status,
        pol.policyPdfUrl
    )
    FROM Policy pol
    JOIN pol.payment pay
    JOIN pay.quote q
    JOIN q.proposal prop
    JOIN prop.customer cust
    JOIN prop.vehicle v
    LEFT JOIN Inspection i ON i.proposal = prop
    WHERE prop.id = ?1
""")
    PolicyResponseDto getByProposalId(Long proposalId);


    @Query("""
    SELECT new com.springboot.AutomobileInsurance.dto.response.PolicyResponseDto(
        pol.id,
        pol.policyNumber,
        cust.name,
        v.registrationNo,
        v.make,
        v.model,
        prop.policyType,
        i.approvedIdv,
        pol.totalPremium,
        pol.startDate,
        pol.endDate,
        pol.status,
        pol.policyPdfUrl
    )
    FROM Policy pol
    JOIN pol.payment pay
    JOIN pay.quote q
    JOIN q.proposal prop
    JOIN prop.customer cust
    JOIN prop.vehicle v
    LEFT JOIN Inspection i ON i.proposal = prop
    WHERE q.id = ?1
""")
    PolicyResponseDto getByQuoteId(Long quoteId);


    @Query("""
        SELECT new com.springboot.AutomobileInsurance.dto.response.PolicyResponseDto(
                                    pol.id,
                                    pol.policyNumber,
                                    cust.name,
                                    v.registrationNo,
                                    v.make,
                                    v.model,
                                    prop.policyType,
                                    i.approvedIdv,
                                    pol.totalPremium,
                                    pol.startDate,
                                    pol.endDate,
                                    pol.status,
                                    pol.policyPdfUrl
                                )
                                FROM Policy pol
                                JOIN pol.payment pay
                                JOIN pay.quote q
                                JOIN q.proposal prop
                                JOIN prop.customer cust
                                JOIN prop.vehicle v
                                LEFT JOIN Inspection i ON i.proposal = prop
                                ORDER BY pol.id DESC
    """)
    List<PolicyResponseDto> findAllPolicyDto();




    @Query("""
    SELECT new com.springboot.AutomobileInsurance.dto.response.PolicyResponseDtoV2(
        pol.id,
        pol.policyNumber,
        cust.name,
        v.registrationNo,
        v.make,
        v.model,
        prop.policyType,
       i.approvedIdv,
        pol.startDate,
        pol.endDate,
        pol.totalPremium,
        pol.status,
        pol.policyPdfUrl
    )
    FROM Policy pol
    JOIN pol.payment pay
    JOIN pay.quote q
    JOIN q.proposal prop
    JOIN prop.customer cust
    JOIN cust.user u
    JOIN prop.vehicle v
    LEFT JOIN Inspection i ON i.proposal = prop
    WHERE u.username = ?1
    ORDER BY pol.id DESC
""")
    List<PolicyResponseDtoV2> findCustomerPolicyByUsername(String name);


    @Query("""
        SELECT new com.springboot.AutomobileInsurance.dto.response.PolicyResponseDto(
            pol.id,
            pol.policyNumber,
            cust.name,
            v.registrationNo,
            v.make,
            v.model,
            prop.policyType,
            i.approvedIdv,
            pol.totalPremium,
            pol.startDate,
            pol.endDate,
            pol.status,
            pol.policyPdfUrl
        )
        FROM Policy pol
        JOIN pol.payment pay
        JOIN pay.quote q
        JOIN q.proposal prop
        JOIN prop.customer cust
        JOIN prop.vehicle v
        LEFT JOIN Inspection i ON i.proposal = prop
        ORDER BY pol.id DESC
    """)
    Page<PolicyResponseDto> findAllPoliciesPaginated(Pageable pageable);
}