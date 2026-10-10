package com.springboot.AutomobileInsurance.model;

import com.springboot.AutomobileInsurance.enums.InspectionResult;
import com.springboot.AutomobileInsurance.enums.InspectionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "inspection")
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "proposal_id",referencedColumnName = "id",nullable = false, unique = true)
    private Proposal proposal;

    @ManyToOne
    @JoinColumn(name = "inspector_id",referencedColumnName = "id", nullable = false)
    private Inspector inspector;

    @Enumerated(EnumType.STRING)
    @Column(name = "result",length = 20)
    private InspectionResult result;

    @Column(name = "recommended_idv",  precision = 10, scale = 2)
    private BigDecimal recommendedIdv;

    @Column(name = "inspection_photos_url", length = 500)
    private String inspectionPhotosUrl;

    @Column(name = "inspection_notes", columnDefinition = "TEXT")
    private String inspectionNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false, length = 30)
    private InspectionStatus status;

}