package com.springboot.AutomobileInsurance.controller;

import com.springboot.AutomobileInsurance.dto.request.ProcessPaymentDto;
import com.springboot.AutomobileInsurance.dto.response.PaymentReceiptDto;
import com.springboot.AutomobileInsurance.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/api/payment/process/{quoteId}")
    public void processPayment(@PathVariable Long quoteId,
                                            @Valid @RequestBody ProcessPaymentDto dto) {
        paymentService.processPayment(quoteId, dto);
    }

    @GetMapping("/api/payment/receipt/{paymentId}")
    public PaymentReceiptDto getReceipt(@PathVariable Long paymentId) {
        return paymentService.getReceiptByPaymentId(paymentId);
    }


}