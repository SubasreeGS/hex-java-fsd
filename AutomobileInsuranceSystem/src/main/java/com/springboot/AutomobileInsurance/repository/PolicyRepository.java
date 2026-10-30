package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.dto.response.PolicyResponseDto;
import com.springboot.AutomobileInsurance.dto.response.PolicyResponseDtoV2;
import com.springboot.AutomobileInsurance.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
            pol.totalPremium,
            pol.startDate,
            pol.endDate,
            pol.policyPdfUrl
        )
        FROM Policy pol
        JOIN pol.payment pay
        JOIN pay.quote q
        JOIN q.proposal prop
        JOIN prop.customer cust
        JOIN prop.vehicle v
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
            pol.totalPremium,
            pol.startDate,
            pol.endDate,
            pol.policyPdfUrl
        )
        FROM Policy pol
        JOIN pol.payment pay
        JOIN pay.quote q
        JOIN q.proposal prop
        JOIN prop.customer cust
        JOIN prop.vehicle v
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
            pol.totalPremium,
            pol.startDate,
            pol.endDate,
            pol.policyPdfUrl
        )
        FROM Policy pol
        JOIN pol.payment pay
        JOIN pay.quote q
        JOIN q.proposal prop
        JOIN prop.customer cust
        JOIN prop.vehicle v
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
            pol.totalPremium,
            pol.startDate,
            pol.endDate,
            pol.policyPdfUrl
        )
        FROM Policy pol
        JOIN pol.payment pay
        JOIN pay.quote q
        JOIN q.proposal prop
        JOIN prop.customer cust
        JOIN prop.vehicle v
        ORDER BY pol.id DESC
    """)
    List<PolicyResponseDto> findAllPolicyDto();




    @Query("""
        SELECT new com.springboot.AutomobileInsurance.dto.response.PolicyResponseDtoV2(
            pol.policyNumber,
            c.name,
            v.registrationNo,
            v.make,
            v.model,
            prop.policyType,
            pol.startDate,
            pol.endDate,
            pol.totalPremium,
            pol.policyPdfUrl
        )
        FROM Policy pol
        JOIN pol.payment pay
        JOIN pay.quote q
        JOIN q.proposal prop
        JOIN prop.customer c
        JOIN prop.vehicle v
        JOIN c.user u
        WHERE u.username = ?1
    """)
    List<PolicyResponseDtoV2> findCustomerPolicyByUsername(String name);
}