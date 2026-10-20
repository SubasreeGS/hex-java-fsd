package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.dto.response.InspectionResponseDto;
import com.springboot.AutomobileInsurance.enums.InspectionStatus;
import com.springboot.AutomobileInsurance.model.Inspection;
import com.springboot.AutomobileInsurance.model.Proposal;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, Long> {

    Optional<Inspection> findByProposalId(Long proposalId);

    List<Inspection> findByInspectorId(Long inspectorId);

    List<Inspection> findByStatus(InspectionStatus status);


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
}