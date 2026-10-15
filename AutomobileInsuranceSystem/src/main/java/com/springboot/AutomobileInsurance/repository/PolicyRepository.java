package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {

    Optional<Policy> findByPolicyNumber(String policyNumber);

    Optional<Policy> findByProposalId(Long proposalId);

    Optional<Policy> findByPaymentId(Long paymentId);
}