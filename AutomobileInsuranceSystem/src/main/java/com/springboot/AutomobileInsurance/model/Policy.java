package com.springboot.AutomobileInsurance.model;

import com.springboot.AutomobileInsurance.enums.PolicyStatus;
import jakarta.persistence.*;
import lombok.*;

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

    @OneToOne
    @JoinColumn(name = "payment_id",referencedColumnName = "id", nullable = false, unique = true)
    private Payment payment;

    @ManyToOne
    @JoinColumn(name = "renewal_id",referencedColumnName = "id")
    private Policy renewedFromPolicy;

    @Column(name = "policy_no", nullable = false, unique = true, length = 50)
    private String policyNo;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "policy_doc_url", length = 500)
    private String policyDocUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false, length = 20)
    private PolicyStatus status;

}