package com.springboot.AutomobileInsurance.model;

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
@Table(name = "policy")
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_number", nullable = false, unique = true, length = 50)
    private String policyNumber;

    @OneToOne
    @JoinColumn(name = "proposal_id", referencedColumnName = "id", nullable = false, unique = true)
    private Proposal proposal;

    @OneToOne
    @JoinColumn(name = "payment_id", referencedColumnName = "id", nullable = false, unique = true)
    private Payment payment;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_premium", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPremium;

    @Column(name = "policy_pdf_url", length = 500)
    private String policyPdfUrl;
}