package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.dto.response.PaymentReceiptDto;
import com.springboot.AutomobileInsurance.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("""
SELECT new com.springboot.AutomobileInsurance.dto.response.PaymentReceiptDto(
            pay.id,
            pay.transactionRef,
            q.id,
            prop.id,
            pol.policyNumber,
            prop.customerName,
            prop.registrationNo,
            pay.amount,
            pay.paymentDate,
            pay.status
        )
        FROM Payment pay
        JOIN pay.quote q
        JOIN q.proposal prop
        JOIN prop.policy pol
        WHERE pay.id = ?1
""")

    PaymentReceiptDto getReceiptByPaymentId(Long paymentId);

    Optional<Payment> findByQuoteId(Long quoteId);

    Optional<Payment> findByTransactionRef(String transactionRef);

    boolean existsByQuoteId(Long quoteId);


}