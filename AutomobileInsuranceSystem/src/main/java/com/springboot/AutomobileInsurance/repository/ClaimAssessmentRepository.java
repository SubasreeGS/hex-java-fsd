package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.dto.response.InspectorTaskResponseDto;
import com.springboot.AutomobileInsurance.model.ClaimAssessment;
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
        JOIN insp.user u
        JOIN ca.claim c
        JOIN c.policy pol
        JOIN pol.payment pay
        JOIN pay.quote q
        JOIN q.proposal prop
        JOIN prop.vehicle v
        JOIN prop.customer cust
        WHERE u.username =?1
    """)
    List<InspectorTaskResponseDto> findTasksByInspectorUsername(String username);
}