package com.springboot.AutomobileInsurance.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "quote")
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "proposal_id", referencedColumnName = "id", nullable = false, unique = true)
    private Proposal proposal;

    @Column(name = "approved_idv", precision = 10, scale = 2)
    private BigDecimal approvedIdv;

    @Column(name = "third_party_premium", precision = 10, scale = 2)
    private BigDecimal thirdPartyPremium;

    @Column(name = "own_damage_premium", precision = 10, scale = 2)
    private BigDecimal ownDamagePremium;

    @Column(name = "addon_premium", precision = 10, scale = 2)
    private BigDecimal addonPremium;

    @Column(name = "total_payable", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPayable;
}