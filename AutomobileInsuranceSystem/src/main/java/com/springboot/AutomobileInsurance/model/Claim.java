package com.springboot.AutomobileInsurance.model;

import com.springboot.AutomobileInsurance.enums.ClaimStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "claim")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "policy_id",referencedColumnName = "id", nullable = false)
    private Policy policy;

    @ManyToOne
    @JoinColumn(name = "officer_id",referencedColumnName = "id")
    private Officer officer;

    @Column(name = "claim_no", nullable = false, unique = true, length = 50)
    private String claimNo;

    @Column(name = "incident_date", nullable = false)
    private LocalDateTime incidentDate;

    @Column(name = "incident_description", columnDefinition = "TEXT", nullable = false)
    private String incidentDescription;

    @Column(name = "claimed_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal claimedAmount;

    @Column(name = "approved_amount", precision = 10, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false, length = 30)
    private ClaimStatus status;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


}