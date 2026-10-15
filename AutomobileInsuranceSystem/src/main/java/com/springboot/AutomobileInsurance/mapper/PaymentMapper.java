package com.springboot.AutomobileInsurance.mapper;

import com.springboot.AutomobileInsurance.dto.response.PaymentReceiptDto;
import com.springboot.AutomobileInsurance.model.Payment;
import com.springboot.AutomobileInsurance.model.Policy;

public class PaymentMapper {

    public static PaymentReceiptDto convertEntityToReceiptDto(Payment payment, Policy policy) {
        return new PaymentReceiptDto(
                payment.getId(),
                payment.getTransactionRef(),
                payment.getQuote().getId(),
                payment.getQuote().getProposal().getId(),
                policy != null ? policy.getPolicyNumber() : null,
                payment.getQuote().getProposal().getCustomer().getName(),
                payment.getQuote().getProposal().getVehicle().getRegistrationNo(),
                payment.getAmount(),
                payment.getPaymentDate(),
                payment.getStatus()
        );
    }
}