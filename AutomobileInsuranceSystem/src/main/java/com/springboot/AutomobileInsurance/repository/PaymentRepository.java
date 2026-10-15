package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByQuoteId(Long quoteId);

    Optional<Payment> findByTransactionRef(String transactionRef);

    boolean existsByQuoteId(Long quoteId);
}