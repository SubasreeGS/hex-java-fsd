package com.springboot.AutomobileInsurance.model;

import com.springboot.AutomobileInsurance.enums.PolicyStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity

public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_number", nullable = false, unique = true, length = 50)
    private String policyNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_premium", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPremium;

    @Column(name = "policy_pdf_url", length = 600)
    private String policyPdfUrl;

    @Enumerated(EnumType.STRING)
    private PolicyStatus status;

    @OneToOne
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;



}