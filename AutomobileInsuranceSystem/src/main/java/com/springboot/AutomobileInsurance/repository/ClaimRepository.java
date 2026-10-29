package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.dto.response.ClaimResponseDto;
import com.springboot.AutomobileInsurance.model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Optional<Claim> findByClaimNo(String claimNo);

    List<Claim> findByPolicyId(Long policyId);

    boolean existsByClaimNo(String claimNo);

    @Query("""
        SELECT new com.springboot.AutomobileInsurance.dto.response.ClaimResponseDto(
            c.claimNo,
            pol.policyNumber,
            c.incidentDate,
            c.incidentDescription,
            c.claimedAmount,
            c.approvedAmount,
            c.status,
            off.name,
            c.createdAt,
            c.updatedAt
        )
        FROM Claim c
        JOIN c.policy pol
        JOIN pol.payment pay
        JOIN pay.quote q
        JOIN q.proposal prop
        JOIN prop.customer cust
        JOIN cust.user u
        LEFT JOIN c.officer off
        WHERE u.username = ?1
    """)
    List<ClaimResponseDto> findClaimsByCustomerUsername(String username);
}