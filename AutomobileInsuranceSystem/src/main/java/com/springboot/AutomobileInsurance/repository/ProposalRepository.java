package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.enums.ProposalStatus;
import com.springboot.AutomobileInsurance.model.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    List<Proposal> findByCustomerId(Long customerId);

    List<Proposal> findByOfficerId(Long officerId);

    List<Proposal> findByStatus(ProposalStatus status);

    //boolean existsByVehicleIdAndStatusNotIn(Long vehicleId, List<ProposalStatus> statuses);


}