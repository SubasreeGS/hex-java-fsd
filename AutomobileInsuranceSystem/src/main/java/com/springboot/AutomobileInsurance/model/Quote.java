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

public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tp_premium", nullable = false, precision = 10, scale = 2)
    private BigDecimal tpPremium;

    @Column(name = "od_premium", nullable = false, precision = 10, scale = 2)
    private BigDecimal odPremium;

    @Column(name = "zero_dep_premium", nullable = false, precision = 10, scale = 2)
    private BigDecimal zeroDepPremium;

    @Column(name = "engine_protect_premium", nullable = false, precision = 10, scale = 2)
    private BigDecimal engineProtectPremium;

    @Column(name = "roadside_assist_premium", nullable = false, precision = 10, scale = 2)
    private BigDecimal roadsideAssistPremium;

    @Column(name = "net_premium", nullable = false, precision = 10, scale = 2)
    private BigDecimal netPremium;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "total_premium", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPremium;

    @Column(name = "valid_until", nullable = false)
    private LocalDate validUntil;

    @OneToOne
    @JoinColumn(name = "proposal_id",nullable = false)
    private Proposal proposal;
}