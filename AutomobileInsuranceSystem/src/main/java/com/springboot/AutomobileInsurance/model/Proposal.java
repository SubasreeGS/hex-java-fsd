package com.springboot.AutomobileInsurance.model;

import com.springboot.AutomobileInsurance.enums.PolicyType;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity

public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rc_doc_url", nullable = false, length = 500)
    private String rcDocUrl;

    @Enumerated(EnumType.STRING)
    private PolicyType policyType;

    @Column(name = "is_zero_dep")
    private boolean isZeroDep;

    @Column(name = "is_engine_protect")
    private boolean isEngineProtect;

    @Column(name = "is_roadside_assist")
    private boolean isRoadsideAssist;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProposalStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "rejection_remarks", length = 1000)
    private String rejectionRemarks;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "officer_id")
    private Officer officer;

    @ManyToOne
    @JoinColumn(name = "rto_vehicle_id", nullable = false)
    private RtoVehicle vehicle;

}