package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.dto.response.InspectorTaskResponseDto;
import com.springboot.AutomobileInsurance.model.ClaimAssessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimAssessmentRepository extends JpaRepository<ClaimAssessment, Long> {

    Optional<ClaimAssessment> findByClaimId(Long claimId);

    List<ClaimAssessment> findByInspectorId(Long inspectorId);

    boolean existsByClaimId(Long claimId);

    @Query("""
        SELECT new com.springboot.AutomobileInsurance.dto.response.InspectorTaskResponseDto(
            ca.id,
            c.claimNo,
            v.registrationNo,
            v.make,
            v.model,
            v.manufactureYear,
            v.vinNo,
            v.fuelType,
            cust.name,
            cust.phoneNo,
            cust.address,
            c.incidentDate,
            c.incidentDescription,
            c.claimedAmount,
            ca.status
        )
        FROM ClaimAssessment ca
        JOIN ca.inspector insp
        JOIN insp.user inspUser
        JOIN ca.claim c
        JOIN c.policy pol
        LEFT JOIN pol.payment pay
        LEFT JOIN pay.quote q
        LEFT JOIN q.proposal prop
        LEFT JOIN prop.vehicle v
        LEFT JOIN prop.customer cust
        WHERE inspUser.username=?1
        ORDER BY ca.id DESC
    """)
    Page<InspectorTaskResponseDto> findTasksByInspectorUsername(String username, Pageable pageable);


}