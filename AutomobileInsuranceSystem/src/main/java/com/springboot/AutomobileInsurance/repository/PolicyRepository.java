package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.dto.response.PolicyResponseDto;
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
            prop.customerName,
            prop.registrationNo,
            prop.vehicleMake,
            prop.vehicleModel,
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
        WHERE pol.policyNumber = ?1
""")
    PolicyResponseDto getByPolicyNumber(String policyNumber);


@Query("""
SELECT new com.springboot.AutomobileInsurance.dto.response.PolicyResponseDto(
            pol.id,
            pol.policyNumber,
            prop.customerName,
            prop.registrationNo,
            prop.vehicleMake,
            prop.vehicleModel,
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
        WHERE prop.id = ?1
""")
    PolicyResponseDto getByProposalId(Long proposalId);


@Query("""
SELECT new com.springboot.AutomobileInsurance.dto.response.PolicyResponseDto(
        pol.id,
        pol.policyNumber,
        prop.customerName,
        prop.registrationNo,
        prop.vehicleMake,
        prop.vehicleModel,
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
    WHERE q.id = ?1
""")
    PolicyResponseDto getByQuoteId(Long quoteId);


@Query("""
SELECT new com.springboot.AutomobileInsurance.dto.response.PolicyResponseDto(
            pol.id,
            pol.policyNumber,
            prop.customerName,
            prop.registrationNo,
            prop.vehicleMake,
            prop.vehicleModel,
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
        ORDER BY pol.id DESC
""")
    List<PolicyResponseDto> findAllPolicyDto();

    Optional<Policy> findByProposalId(Long proposalId);

    Optional<Policy> findByPaymentId(Long paymentId);



}