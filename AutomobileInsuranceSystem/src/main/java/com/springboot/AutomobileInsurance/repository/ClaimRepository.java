package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.dto.response.*;
import com.springboot.AutomobileInsurance.enums.ClaimStatus;
import com.springboot.AutomobileInsurance.model.Claim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Optional<Claim> findByClaimNo(String claimNo);

    List<Claim> findByPolicyId(Long policyId);

    boolean existsByClaimNo(String claimNo);

    @Query("""
        SELECT new com.springboot.AutomobileInsurance.dto.response.ClaimResponseDto(
            c.id,
            c.claimNo,
            p.policyNumber,
            c.incidentDate,
            c.incidentDescription,
            c.claimedAmount,
            c.approvedAmount,
            c.status,
            offUser.username,
            c.createdAt,
            c.updatedAt
        )
        FROM Claim c
        JOIN c.policy p
        JOIN p.payment pay
        JOIN pay.quote q
        JOIN q.proposal prop
        JOIN prop.customer cust
        JOIN cust.user u
        LEFT JOIN c.officer off
        LEFT JOIN off.user offUser
        LEFT JOIN ClaimAssessment ca ON ca.claim = c
        WHERE u.username = ?1
        ORDER BY c.id DESC
    """)
    List<ClaimResponseDto> findClaimsByCustomerUsername(String username);


    @Query("""
        SELECT new com.springboot.AutomobileInsurance.dto.response.ClaimResponseDtoV2(
            c.id,
            c.claimNo,
            p.policyNumber,
            c.incidentDate,
            c.incidentDescription,
            c.claimedAmount,
            c.approvedAmount,
            c.status,
            offUser.username,
            inspUser.username,
            c.createdAt,
            c.updatedAt
        )
        FROM Claim c
        JOIN c.policy p
        LEFT JOIN c.officer off
        LEFT JOIN off.user offUser
        LEFT JOIN ClaimAssessment ca ON ca.claim = c
        LEFT JOIN ca.inspector insp
        LEFT JOIN insp.user inspUser
        ORDER BY c.id DESC
    """)
    Page<ClaimResponseDtoV2> findAllClaimsForOfficer(Pageable pageable);


    @Query("""
    SELECT new com.springboot.AutomobileInsurance.dto.response.ClaimSettlementReceiptDto(
        c.id,
        c.claimNo,
        pol.policyNumber,
        cust.name,
        cust.phoneNo,
        v.registrationNo,
        pay.transactionRef,
        c.claimedAmount,
        c.approvedAmount,
        c.settlementRef,
        c.settledDate,
        c.status,
        offUser.username
    )
    FROM Claim c
    JOIN c.policy pol
    JOIN pol.payment pay
    JOIN pay.quote q
    JOIN q.proposal prop
    JOIN prop.customer cust
    JOIN prop.vehicle v
    LEFT JOIN c.officer off
    LEFT JOIN off.user offUser
    WHERE c.id = ?1
""")
    ClaimSettlementReceiptDto getSettlementReceiptByClaimId(Long claimId);

    @Query("SELECT new com.springboot.AutomobileInsurance.dto.response.StatDto(CAST(c.status AS string), COUNT(c)) FROM Claim c GROUP BY c.status")
    List<StatDto> getClaimsCountByStatus();


    @Query("SELECT COALESCE(SUM(c.approvedAmount), 0.0) FROM Claim c WHERE c.status = 'SETTLED'")
    Double getTotalSettledPayout();


    @Query("""
        select
            cl.id as id,
            cl.claimNo as claimNo,
            cust.name as customerName,
            cl.claimedAmount as claimedAmount,
            cl.status as status,
            cl.createdAt as createdAt
        from Claim cl
        left join cl.policy pol
        left join pol.payment pay
        left join pay.quote q
        left join q.proposal prop
        left join prop.customer cust
        where (?1 IS NULL OR cl.status = ?1)
          and (?2 IS NULL OR cl.createdAt >= ?2)
          and (?3 IS NULL OR cl.createdAt <= ?3)
        order by cl.createdAt desc
    """)
    Page<ClaimRespDto> dynamicFilterClaims(
            ClaimStatus status,
            Instant fromDate,
            Instant toDate,
            Pageable pageable
    );


    @Query("""
        select
            cl.id as id,
            cl.claimNo as claimNo,
            cust.name as customerName,
            veh.registrationNo as vehicleNumber,
            cl.claimedAmount as claimedAmount,
            cl.status as status,
            cl.incidentDate as incidentDate
        from Claim cl
        left join cl.policy pol
        left join pol.payment pay
        left join pay.quote q
        left join q.proposal prop
        left join prop.customer cust
        left join prop.vehicle veh
        where (?1 IS NULL OR cl.status = ?1)
          and (?2 IS NULL OR cl.incidentDate >= ?2)
          and (?3 IS NULL OR cl.incidentDate <= ?3)
        order by cl.id desc
    """)
    Page<InspectorClaimRespDto> dynamicFilterInspectorClaims(
            ClaimStatus status,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    );

    @Query("SELECT COUNT(c) FROM Claim c WHERE c.status = com.springboot.AutomobileInsurance.enums.ClaimStatus.UNDER_ASSESSMENT")
    long countPendingClaimAssessments();
}