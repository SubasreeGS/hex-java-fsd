package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.model.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {

    Optional<Quote> findByProposalId(Long proposalId);

    boolean existsByProposalId(Long proposalId);
}