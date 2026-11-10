package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.dto.response.InspectionResponseDto;
import com.springboot.AutomobileInsurance.dto.response.InspectorProposalRespDto;
import com.springboot.AutomobileInsurance.enums.InspectionStatus;
import com.springboot.AutomobileInsurance.enums.PolicyType;
import com.springboot.AutomobileInsurance.model.Inspection;
import com.springboot.AutomobileInsurance.model.Proposal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, Long> {

    Optional<Inspection> findByProposalId(Long proposalId);

    List<Inspection> findByInspectorId(Long inspectorId);

@Query("""
SELECT new com.springboot.AutomobileInsurance.dto.response.InspectionResponseDto(
            p.id,
            i.result,
            i.recommendedIdv,
            i.inspectionPhotosUrl,
            i.inspectionNotes,
            i.status
        )
        FROM Inspection i
        JOIN i.proposal p
        JOIN i.inspector ins
        JOIN ins.user u
        WHERE u.username = ?1
""")
    List<InspectionResponseDto> getReportsByInspectorUsername(String inspectorUsername, Pageable pageable);


@Query("""
SELECT new com.springboot.AutomobileInsurance.dto.response.InspectionResponseDto(
            p.id,
            i.result,
            i.recommendedIdv,
            i.inspectionPhotosUrl,
            i.inspectionNotes,
            i.status
        )
        FROM Inspection i
        JOIN i.proposal p
        JOIN i.inspector ins
        JOIN ins.user u
        WHERE i.status = ?1
          AND u.username = ?2

""")
    List<InspectionResponseDto> getInspectionsByStatus(InspectionStatus status, String inspectorUsername);


    @Query("""
        select
            i.id as inspectionId,
            p.id as proposalId,
            c.name as customerName,
            veh.registrationNo as vehicleNumber,
            p.policyType as policyType,
            i.status as inspectionStatus,
            p.createdAt as submittedOn
        from Inspection i
        left join i.proposal p
        left join p.customer c
        left join p.vehicle veh
        where i.inspector.id = ?1
          and (?2 IS NULL OR p.policyType = ?2)
          and (?3 IS NULL OR i.status = ?3)
          and (?4 IS NULL OR p.createdAt >= ?4)
          and (?5 IS NULL OR p.createdAt <= ?5)
        order by i.id desc
    """)
    Page<InspectorProposalRespDto> dynamicFilterInspectorProposals(
            Long inspectorId,
            PolicyType policyType,
            InspectionStatus status,
            Instant fromDate,
            Instant toDate,
            Pageable pageable
    );

    @Query("SELECT COUNT(i) FROM Inspection i WHERE i.inspector.id = ?1 AND i.status IN (com.springboot.AutomobileInsurance.enums.InspectionStatus.SCHEDULED, com.springboot.AutomobileInsurance.enums.InspectionStatus.IN_PROGRESS)")
    long countPendingSurveysByInspector(Long inspectorId);

    @Query("SELECT COUNT(i) FROM Inspection i WHERE i.inspector.id = ?1 AND i.status = com.springboot.AutomobileInsurance.enums.InspectionStatus.COMPLETED")
    long countCompletedSurveysByInspector(Long inspectorId);

}