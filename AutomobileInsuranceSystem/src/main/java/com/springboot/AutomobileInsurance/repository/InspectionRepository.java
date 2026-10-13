package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.enums.InspectionStatus;
import com.springboot.AutomobileInsurance.model.Inspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, Long> {

    Optional<Inspection> findByProposalId(Long proposalId);

    List<Inspection> findByInspectorId(Long inspectorId);

    List<Inspection> findByStatus(InspectionStatus status);
}