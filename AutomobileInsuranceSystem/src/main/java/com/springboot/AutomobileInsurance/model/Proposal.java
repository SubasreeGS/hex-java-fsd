package com.springboot.AutomobileInsurance.model;

import com.springboot.AutomobileInsurance.enums.PolicyType;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "proposal")
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id",nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "officer_id",referencedColumnName = "id")
    private Officer officer;

    @ManyToOne
    @JoinColumn(name = "rto_vehicle_id", referencedColumnName = "id", nullable = false)
    private RtoVehicle vehicle;

    @Column(name = "rc_doc_url", nullable = false, length = 500)
    private String rcDocUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_type", nullable = false, length = 30)
    private PolicyType policyType;

    @Column(name = "is_zero_dep", nullable = false)
    private boolean isZeroDep;

    @Column(name = "is_engine_protect", nullable = false)
    private boolean isEngineProtect;

    @Column(name = "is_roadside_assist", nullable = false)
    private boolean isRoadsideAssist;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false, length = 40)
    private ProposalStatus status;


}