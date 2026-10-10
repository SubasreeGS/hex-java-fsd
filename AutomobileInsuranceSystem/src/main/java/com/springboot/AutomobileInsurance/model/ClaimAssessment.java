package com.springboot.AutomobileInsurance.model;

import com.springboot.AutomobileInsurance.enums.AssessmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "claim_assessment")
public class ClaimAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "claim_id",referencedColumnName = "id", nullable = false, unique = true)
    private Claim claim;

    @ManyToOne
    @JoinColumn(name = "inspector_id",referencedColumnName = "id", nullable = false)
    private Inspector inspector;

    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "damage_photos_url", length = 500)
    private String damagePhotosUrl;

    @Column(name = "assessment_notes", columnDefinition = "TEXT")
    private String assessmentNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private AssessmentStatus status;


}