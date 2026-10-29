package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.ProcessPaymentDto;
import com.springboot.AutomobileInsurance.dto.response.PaymentReceiptDto;
import com.springboot.AutomobileInsurance.enums.PaymentStatus;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.mapper.PaymentMapper;
import com.springboot.AutomobileInsurance.model.Payment;
import com.springboot.AutomobileInsurance.model.Policy;
import com.springboot.AutomobileInsurance.model.Proposal;
import com.springboot.AutomobileInsurance.model.Quote;
import com.springboot.AutomobileInsurance.repository.PaymentRepository;
import com.springboot.AutomobileInsurance.repository.PolicyRepository;
import com.springboot.AutomobileInsurance.repository.ProposalRepository;
import com.springboot.AutomobileInsurance.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final QuoteRepository quoteRepository;
    private final ProposalRepository proposalRepository;
    private final PolicyRepository policyRepository;

    public void processPayment(Long quoteId, ProcessPaymentDto dto) {
        // Fetch and validate quote
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found"));

        // Validate exact amount paid matches quote total
        if (dto.amount().compareTo(quote.getTotalPremium()) != 0) {
            throw new InvalidCallException("Payment amount does not match quote total");
        }

        Proposal proposal = quote.getProposal();

        // Save Payment entity
        Payment payment = new Payment();
        payment.setQuote(quote);
        payment.setTransactionRef(dto.transactionRef());
        payment.setAmount(dto.amount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus(PaymentStatus.SUCCESS);
        Payment savedPayment = paymentRepository.save(payment);

        // Automatically issue Policy upon successful payment
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusYears(1).minusDays(1);
        String policyNumber = "POL-" + java.time.Year.now() + "-" + (int)((Math.random()*10000));

        Policy policy = new Policy();
        policy.setPolicyNumber(policyNumber);
        policy.setPayment(savedPayment);
        policy.setStartDate(startDate);
        policy.setEndDate(endDate);
        policy.setTotalPremium(quote.getTotalPremium());
        policy.setPolicyPdfUrl("https://storage.automobile-insurance.com/policies/" + policyNumber + ".pdf");
        Policy savedPolicy = policyRepository.save(policy);

        // Update Proposal status to POLICY_ISSUED
        proposal.setStatus(ProposalStatus.POLICY_ISSUED);
        proposalRepository.save(proposal);

    }

    public PaymentReceiptDto getReceiptByPaymentId(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found"));

        return paymentRepository.getReceiptByPaymentId(paymentId);
    }




}