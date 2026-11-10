package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.ProcessPaymentDto;
import com.springboot.AutomobileInsurance.dto.response.PaymentReceiptDto;
import com.springboot.AutomobileInsurance.enums.PaymentStatus;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.model.Payment;
import com.springboot.AutomobileInsurance.model.Policy;
import com.springboot.AutomobileInsurance.model.Proposal;
import com.springboot.AutomobileInsurance.model.Quote;
import com.springboot.AutomobileInsurance.repository.PaymentRepository;
import com.springboot.AutomobileInsurance.repository.PolicyRepository;
import com.springboot.AutomobileInsurance.repository.ProposalRepository;
import com.springboot.AutomobileInsurance.repository.QuoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Quote quote;
    private Proposal proposal;
    private Payment savedPayment;
    private ProcessPaymentDto paymentDto;
    private PaymentReceiptDto receipt;

    @BeforeEach
    void setUp() {
        proposal = new Proposal();
        proposal.setId(83L);
        proposal.setStatus(ProposalStatus.PAYMENT_PENDING);

        quote = new Quote();
        quote.setId(42L);
        quote.setTotalPremium(new BigDecimal("18450.75"));
        quote.setValidUntil(LocalDate.of(2026, 12, 31));
        quote.setProposal(proposal);

        savedPayment = new Payment();
        savedPayment.setId(57L);
        savedPayment.setTransactionRef("TXN-2026-0042");
        savedPayment.setAmount(new BigDecimal("18450.75"));
        savedPayment.setPaymentDate(LocalDateTime.of(2026, 11, 9, 14, 30));
        savedPayment.setStatus(PaymentStatus.SUCCESS);
        savedPayment.setQuote(quote);

        paymentDto = new ProcessPaymentDto("TXN-2026-0042", new BigDecimal("18450.75"), "UPI");
        receipt = new PaymentReceiptDto(
                57L,
                "TXN-2026-0042",
                42L,
                83L,
                "POL-2026-0042",
                "Anika Rao",
                "KA03MN4821",
                new BigDecimal("18450.75"),
                LocalDateTime.of(2026, 11, 9, 14, 30),
                PaymentStatus.SUCCESS
        );
    }

    @Test
    void processPayment_savesPaymentAndIssuesPolicy() {
        when(quoteRepository.findById(42L)).thenReturn(Optional.of(quote));
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        LocalDate beforeProcessing = LocalDate.now();
        paymentService.processPayment(42L, paymentDto);
        LocalDate afterProcessing = LocalDate.now();

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        ArgumentCaptor<Policy> policyCaptor = ArgumentCaptor.forClass(Policy.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        verify(policyRepository).save(policyCaptor.capture());
        Payment payment = paymentCaptor.getValue();
        assertEquals(quote, payment.getQuote());
        assertEquals("TXN-2026-0042", payment.getTransactionRef());
        assertEquals(new BigDecimal("18450.75"), payment.getAmount());
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertNotNull(payment.getPaymentDate());

        Policy policy = policyCaptor.getValue();
        assertTrue(policy.getStartDate().compareTo(beforeProcessing) >= 0);
        assertTrue(policy.getStartDate().compareTo(afterProcessing) <= 0);
        assertEquals(policy.getStartDate().plusYears(1).minusDays(1), policy.getEndDate());
        assertTrue(policy.getPolicyNumber().matches("POL-\\d{4}-\\d+"));
        assertEquals(savedPayment, policy.getPayment());
        assertEquals(quote.getTotalPremium(), policy.getTotalPremium());
        assertTrue(policy.getPolicyPdfUrl().contains(policy.getPolicyNumber()));

        assertEquals(ProposalStatus.POLICY_ISSUED, proposal.getStatus());
        verify(proposalRepository).save(proposal);
    }

    @Test
    void processPayment_throwsWhenQuoteDoesNotExist() {
        when(quoteRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.processPayment(404L, paymentDto)
        );

        verify(quoteRepository).findById(404L);
        verifyNoInteractions(paymentRepository, policyRepository, proposalRepository);
    }

    @Test
    void processPayment_throwsWhenAmountDoesNotMatchQuoteTotal() {
        when(quoteRepository.findById(42L)).thenReturn(Optional.of(quote));
        ProcessPaymentDto mismatchedPayment =
                new ProcessPaymentDto("TXN-2026-0042", new BigDecimal("18450.74"), "UPI");

        assertThrows(
                InvalidCallException.class,
                () -> paymentService.processPayment(42L, mismatchedPayment)
        );

        verify(paymentRepository, never()).save(any(Payment.class));
        verifyNoInteractions(policyRepository, proposalRepository);
    }

    @Test
    void processPayment_propagatesPaymentSaveFailure() {
        when(quoteRepository.findById(42L)).thenReturn(Optional.of(quote));
        when(paymentRepository.save(any(Payment.class)))
                .thenThrow(new IllegalStateException("payment could not be saved"));

        assertThrows(IllegalStateException.class, () -> paymentService.processPayment(42L, paymentDto));

        verify(paymentRepository).save(any(Payment.class));
        verifyNoInteractions(policyRepository, proposalRepository);
    }

    @Test
    void getReceiptByPaymentId_returnsReceipt() {
        when(paymentRepository.findById(57L)).thenReturn(Optional.of(savedPayment));
        when(paymentRepository.getReceiptByPaymentId(57L)).thenReturn(receipt);

        PaymentReceiptDto response = paymentService.getReceiptByPaymentId(57L);

        assertEquals(receipt, response);
        verify(paymentRepository).findById(57L);
        verify(paymentRepository).getReceiptByPaymentId(57L);
    }

    @Test
    void getReceiptByPaymentId_throwsWhenPaymentDoesNotExist() {
        when(paymentRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.getReceiptByPaymentId(404L)
        );

        verify(paymentRepository).findById(404L);
        verify(paymentRepository, never()).getReceiptByPaymentId(404L);
    }

    @Test
    void getReceiptByPaymentId_propagatesReceiptQueryFailure() {
        when(paymentRepository.findById(57L)).thenReturn(Optional.of(savedPayment));
        when(paymentRepository.getReceiptByPaymentId(57L))
                .thenThrow(new IllegalStateException("receipt query failed"));

        assertThrows(IllegalStateException.class, () -> paymentService.getReceiptByPaymentId(57L));

        verify(paymentRepository).findById(57L);
        verify(paymentRepository).getReceiptByPaymentId(57L);
    }
}
